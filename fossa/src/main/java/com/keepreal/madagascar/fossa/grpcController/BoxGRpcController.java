package com.keepreal.madagascar.fossa.grpcController;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.fossa.AnswerQuestionRequest;
import com.keepreal.madagascar.fossa.AnswerQuestionResponse;
import com.keepreal.madagascar.fossa.BoxServiceGrpc;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.model.QuestionInfo;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class BoxGRpcController extends BoxServiceGrpc.BoxServiceImplBase {

    private final FeedInfoService feedInfoService;

    public BoxGRpcController(FeedInfoService feedInfoService) {
        this.feedInfoService = feedInfoService;
    }

    @Override
    public void answerQuestion(AnswerQuestionRequest request, StreamObserver<AnswerQuestionResponse> responseObserver) {
        String questionId = request.getId();
        String userId = request.getUserId();
        String answer = request.getAnswer();
        boolean publicVisible = request.getPublicVisible();
        ProtocolStringList visibleMembershipIdsList = request.getVisibleMembershipIdsList();

        FeedInfo feedInfo = feedInfoService.findFeedInfoById(questionId, false);
        if (!userId.equals(feedInfo.getHostId())) {
//            responseObserver.onCompleted();
        }

        QuestionInfo questionInfo = (QuestionInfo) feedInfo.getMediaInfos().get(0);
        questionInfo.setAnswer(answer);
        questionInfo.setPublicVisible(publicVisible);
        if (publicVisible) {
            feedInfo.setMembershipIds(visibleMembershipIdsList);
        }
        feedInfoService.update(feedInfo);

        responseObserver.onNext(AnswerQuestionResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build());
        responseObserver.onCompleted();
    }
}
