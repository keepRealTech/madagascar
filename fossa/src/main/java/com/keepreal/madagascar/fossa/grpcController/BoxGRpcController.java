package com.keepreal.madagascar.fossa.grpcController;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.fossa.AnswerQuestionRequest;
import com.keepreal.madagascar.fossa.BoxServiceGrpc;
import com.keepreal.madagascar.fossa.CommonResponse;
import com.keepreal.madagascar.fossa.CreateOrUpdateBoxRequest;
import com.keepreal.madagascar.fossa.CreateOrUpdateBoxResponse;
import com.keepreal.madagascar.fossa.model.BoxInfo;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.model.QuestionInfo;
import com.keepreal.madagascar.fossa.service.BoxInfoService;
import com.keepreal.madagascar.fossa.service.FeedEventProducerService;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class BoxGRpcController extends BoxServiceGrpc.BoxServiceImplBase {

    private final FeedInfoService feedInfoService;
    private final BoxInfoService boxInfoService;
    private final FeedEventProducerService feedEventProducerService;

    public BoxGRpcController(FeedInfoService feedInfoService,
                             FeedEventProducerService feedEventProducerService) {
        this.feedInfoService = feedInfoService;
        this.boxInfoService = boxInfoService;
        this.feedEventProducerService = feedEventProducerService;
    }

    @Override
    public void answerQuestion(AnswerQuestionRequest request, StreamObserver<CommonResponse> responseObserver) {
        String questionId = request.getId();
        String userId = request.getUserId();
        String answer = request.getAnswer();
        boolean publicVisible = request.getPublicVisible();
        ProtocolStringList visibleMembershipIdsList = request.getVisibleMembershipIdsList();

        FeedInfo feedInfo = feedInfoService.findFeedInfoById(questionId, false);

        QuestionInfo questionInfo = (QuestionInfo) feedInfo.getMediaInfos().get(0);
        questionInfo.setAnswer(answer);
        questionInfo.setPublicVisible(publicVisible);
        if (publicVisible) {
            feedInfo.setMembershipIds(visibleMembershipIdsList);
        }
        FeedInfo update = feedInfoService.update(feedInfo);
        this.feedEventProducerService.produceUpdateFeedEventAsync(update);

        responseObserver.onNext(CommonResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void putBox(CreateOrUpdateBoxRequest request, StreamObserver<CreateOrUpdateBoxResponse> responseObserver) {
        BoxInfo boxInfo = new BoxInfo();
        boxInfo.setIslandId(request.getIslandId());
        boxInfo.setEnabled(request.getEnabled());
        boxInfo.setMembershipIds(String.join(",", request.getMembershipIdsList()));

        BoxInfo update = this.boxInfoService.createOrUpdate(boxInfo);

        responseObserver.onNext(CreateOrUpdateBoxResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setMessage(this.boxInfoService.getBoxMessage(update))
                .build());
    }
}
