package com.keepreal.madagascar.fossa.grpcController;

import com.google.protobuf.ProtocolStringList;
import com.google.protobuf.UInt64Value;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.fossa.AnswerQuestionRequest;
import com.keepreal.madagascar.fossa.BoxServiceGrpc;
import com.keepreal.madagascar.fossa.CommonResponse;
import com.keepreal.madagascar.fossa.CreateOrUpdateBoxRequest;
import com.keepreal.madagascar.fossa.CreateOrUpdateBoxResponse;
import com.keepreal.madagascar.fossa.QuestionsResponse;
import com.keepreal.madagascar.fossa.RetrieveAnswerMeQuestionsRequest;
import com.keepreal.madagascar.fossa.RetrieveAnsweredAndVisibleQuestionsRequest;
import com.keepreal.madagascar.fossa.RetrieveAskMeQuestionsRequest;
import com.keepreal.madagascar.fossa.RetrieveBoxInfoRequest;
import com.keepreal.madagascar.fossa.RetrieveBoxInfoResponse;
import com.keepreal.madagascar.fossa.model.BoxInfo;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.model.QuestionInfo;
import com.keepreal.madagascar.fossa.service.BoxInfoService;
import com.keepreal.madagascar.fossa.service.FeedEventProducerService;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@GRpcService
public class BoxGRpcController extends BoxServiceGrpc.BoxServiceImplBase {

    private final FeedInfoService feedInfoService;
    private final BoxInfoService boxInfoService;
    private final FeedEventProducerService feedEventProducerService;
    private final MongoTemplate mongoTemplate;

    public BoxGRpcController(FeedInfoService feedInfoService,
                             BoxInfoService boxInfoService,
                             FeedEventProducerService feedEventProducerService,
                             MongoTemplate mongoTemplate) {
        this.feedInfoService = feedInfoService;
        this.boxInfoService = boxInfoService;
        this.feedEventProducerService = feedEventProducerService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void answerQuestion(AnswerQuestionRequest request, StreamObserver<CommonResponse> responseObserver) {
        String questionId = request.getId();
        String answer = request.getAnswer();
        boolean publicVisible = request.getPublicVisible();
        ProtocolStringList visibleMembershipIdsList = request.getVisibleMembershipIdsList();

        FeedInfo feedInfo = feedInfoService.findFeedInfoById(questionId, false);

        QuestionInfo questionInfo = (QuestionInfo) feedInfo.getMediaInfos().get(0);
        if (!StringUtils.isEmpty(questionInfo.getAnswer())) {
            this.boxInfoService.addAnsweredQuestionCount(feedInfo.getIslandId());
        }
        questionInfo.setAnswer(answer);
        questionInfo.setPublicVisible(publicVisible);
        if (publicVisible) {
            feedInfo.setMembershipIds(visibleMembershipIdsList);
        }
        questionInfo.setAnswerAt(System.currentTimeMillis());
        FeedInfo update = feedInfoService.update(feedInfo);
        this.feedEventProducerService.produceUpdateFeedEventAsync(update);

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
        Query query = this.boxInfoService.retrieveQuestionByCondition(request.getUserId(), request.getAnswered(), request.getPaid(), request.getMembershipId());
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();

        this.queryAndResponseQuestions(query, request.getUserId(), page, pageSize, responseObserver);
    }

    @Override
    public void retrieveBoxInfo(RetrieveBoxInfoRequest request, StreamObserver<RetrieveBoxInfoResponse> responseObserver) {
        String islandId = request.getIslandId();
        BoxInfo boxInfo = this.boxInfoService.getBoxInfoByIslandId(islandId);

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
