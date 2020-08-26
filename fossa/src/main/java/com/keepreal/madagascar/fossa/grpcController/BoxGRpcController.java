package com.keepreal.madagascar.fossa.grpcController;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.fossa.AnswerQuestionRequest;
import com.keepreal.madagascar.fossa.BoxServiceGrpc;
import com.keepreal.madagascar.fossa.CommonResponse;
import com.keepreal.madagascar.fossa.CreateOrUpdateBoxRequest;
import com.keepreal.madagascar.fossa.CreateOrUpdateBoxResponse;
import com.keepreal.madagascar.fossa.IgnoreQuestionRequest;
import com.keepreal.madagascar.fossa.QuestionsResponse;
import com.keepreal.madagascar.fossa.RetrieveAnswerMeQuestionsRequest;
import com.keepreal.madagascar.fossa.RetrieveAnsweredAndVisibleQuestionsRequest;
import com.keepreal.madagascar.fossa.RetrieveAskMeQuestionsRequest;
import com.keepreal.madagascar.fossa.RetrieveBoxInfoRequest;
import com.keepreal.madagascar.fossa.RetrieveBoxInfoResponse;
import com.keepreal.madagascar.fossa.model.BoxInfo;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.model.AnswerInfo;
import com.keepreal.madagascar.fossa.model.MediaInfo;
import com.keepreal.madagascar.fossa.service.BoxInfoService;
import com.keepreal.madagascar.fossa.service.FeedEventProducerService;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.service.PaymentService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@GRpcService
public class BoxGRpcController extends BoxServiceGrpc.BoxServiceImplBase {

    private final FeedInfoService feedInfoService;
    private final BoxInfoService boxInfoService;
    private final FeedEventProducerService feedEventProducerService;
    private final MongoTemplate mongoTemplate;
    private final PaymentService paymentService;

    public BoxGRpcController(FeedInfoService feedInfoService,
                             BoxInfoService boxInfoService,
                             FeedEventProducerService feedEventProducerService,
                             MongoTemplate mongoTemplate,
                             PaymentService paymentService) {
        this.feedInfoService = feedInfoService;
        this.boxInfoService = boxInfoService;
        this.feedEventProducerService = feedEventProducerService;
        this.mongoTemplate = mongoTemplate;
        this.paymentService = paymentService;
    }

    @Override
    public void answerQuestion(AnswerQuestionRequest request, StreamObserver<CommonResponse> responseObserver) {
        String questionId = request.getId();
        String answer = request.getAnswer();
        boolean publicVisible = request.getPublicVisible();
        ProtocolStringList visibleMembershipIdsList = request.getVisibleMembershipIdsList();

        FeedInfo feedInfo = feedInfoService.findFeedInfoById(questionId, false);
        if (!request.getUserId().equals(feedInfo.getHostId())) {
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FORBIDDEN))
                    .build());
            responseObserver.onCompleted();
            return;
        }

        if (!feedInfo.getMultiMediaType().equals(MediaType.MEDIA_QUESTION.name())) {
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_INVALID_ARGUMENT))
                    .build());
            responseObserver.onCompleted();
            return;
        }

        AnswerInfo answerInfo;
        List<MediaInfo> mediaInfos = feedInfo.getMediaInfos();
        if (mediaInfos.isEmpty()) {
            this.boxInfoService.addAnsweredQuestionCount(feedInfo.getIslandId());
            answerInfo = new AnswerInfo();
            answerInfo.setIgnored(false);
            mediaInfos.add(answerInfo);
        } else {
            answerInfo = (AnswerInfo) mediaInfos.get(0);
        }

        answerInfo.setAnswer(answer);
        answerInfo.setAnsweredAt(System.currentTimeMillis());
        answerInfo.setAnswerUserId(request.getUserId());
        answerInfo.setPublicVisible(publicVisible);
        if (publicVisible) {
            feedInfo.setMembershipIds(visibleMembershipIdsList);
        }
        FeedInfo update = feedInfoService.update(feedInfo);
        this.feedEventProducerService.produceUpdateFeedEventAsync(update);
        if (feedInfo.getPriceInCents() != null && feedInfo.getPriceInCents() > 0) {
            this.paymentService.activateFeedPayment(update.getId(), request.getUserId());
        }

        responseObserver.onNext(CommonResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void putBox(CreateOrUpdateBoxRequest request, StreamObserver<CreateOrUpdateBoxResponse> responseObserver) {
        BoxInfo boxInfo = this.boxInfoService.getBoxInfoByIslandId(request.getIslandId());
        if (boxInfo == null) {
            boxInfo = new BoxInfo();
        }
        boxInfo.setIslandId(request.getIslandId());
        boxInfo.setEnabled(request.getEnabled());
        boxInfo.setMembershipIds(String.join(",", request.getMembershipIdsList()));
        boxInfo.setHostId(request.getUserId());

        BoxInfo update = this.boxInfoService.createOrUpdate(boxInfo);

        responseObserver.onNext(CreateOrUpdateBoxResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setMessage(this.boxInfoService.getBoxMessage(update))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveAnsweredAndVisibleQuestion(RetrieveAnsweredAndVisibleQuestionsRequest request, StreamObserver<QuestionsResponse> responseObserver) {
        Query query = this.boxInfoService.retrieveAnswerAndVisibleQuestion(request.getIslandId());
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();

        this.queryAndResponseQuestions(query, request.getUserId(), page, pageSize, responseObserver);
    }

    @Override
    public void retrieveAskMeQuestion(RetrieveAskMeQuestionsRequest request, StreamObserver<QuestionsResponse> responseObserver) {
        Boolean answered = request.hasAnswered() ? request.getAnswered().getValue() : null;
        Boolean paid = request.hasPaid() ? request.getPaid().getValue() : null;
        Boolean hasMembership = request.hasHasMembership() ? request.getHasMembership().getValue() : null;
        Query query = this.boxInfoService.retrieveQuestionByCondition(request.getUserId(), answered, paid, hasMembership);
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();

        this.queryAndResponseQuestions(query, request.getUserId(), page, pageSize, responseObserver);
    }

    @Override
    public void retrieveBoxInfo(RetrieveBoxInfoRequest request, StreamObserver<RetrieveBoxInfoResponse> responseObserver) {
        String islandId = request.getIslandId();
        BoxInfo boxInfo = this.boxInfoService.getBoxInfoByIslandId(islandId);
        if (boxInfo == null) {
            BoxInfo newBox = new BoxInfo();
            newBox.setIslandId(islandId);
            newBox.setEnabled(true);
            newBox.setAnsweredQuestionCount(0);
            newBox.setHostId(request.getHostId());
            boxInfo = this.boxInfoService.createOrUpdate(newBox);
        }

        responseObserver.onNext(RetrieveBoxInfoResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setMessage(this.boxInfoService.getBoxMessage(boxInfo))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveAnswerMeQuestions(RetrieveAnswerMeQuestionsRequest request, StreamObserver<QuestionsResponse> responseObserver) {
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();
        Query query = this.boxInfoService.retrieveAnswerMeQuestion(request.getUserId());

        this.queryAndResponseQuestions(query, request.getUserId(), page, pageSize, responseObserver);
    }

    @Override
    public void ignoreQuestion(IgnoreQuestionRequest request, StreamObserver<CommonResponse> responseObserver) {
        String questionId = request.getQuestionId();

        FeedInfo feedInfo = this.feedInfoService.findFeedInfoById(questionId, false);
        if (!request.getUserId().equals(feedInfo.getHostId())) {
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FORBIDDEN))
                    .build());
            responseObserver.onCompleted();
            return;
        }

        AnswerInfo answerInfo;
        List<MediaInfo> mediaInfos = feedInfo.getMediaInfos();
        if (mediaInfos.isEmpty()) {
            answerInfo = new AnswerInfo();
            mediaInfos.add(answerInfo);
        } else {
            answerInfo = (AnswerInfo) mediaInfos.get(0);
        }
        answerInfo.setIgnored(true);

        feedInfoService.update(feedInfo);

        if (feedInfo.getPriceInCents() != null && feedInfo.getPriceInCents() > 0) {
            this.paymentService.refundWechatPaidFeed(questionId, request.getUserId());
        }
        responseObserver.onNext(CommonResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build());
        responseObserver.onCompleted();
    }

    private void queryAndResponseQuestions(Query query, String userId, int page, int pageSize, StreamObserver<QuestionsResponse> responseObserver) {
        long totalCount = mongoTemplate.count(query, FeedInfo.class);
        List<FeedInfo> feedInfoList = mongoTemplate.find(query.with(PageRequest.of(page, pageSize)), FeedInfo.class);

        List<FeedMessage> feedMessageList = feedInfoList.stream()
                .map(info -> feedInfoService.getFeedMessage(info, userId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        PageResponse pageResponse = PageRequestResponseUtils.buildPageResponse(page, pageSize, totalCount);
        QuestionsResponse feedsResponse = QuestionsResponse.newBuilder()
                .addAllFeed(feedMessageList)
                .setPageResponse(pageResponse)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(feedsResponse);
        responseObserver.onCompleted();
    }

}
