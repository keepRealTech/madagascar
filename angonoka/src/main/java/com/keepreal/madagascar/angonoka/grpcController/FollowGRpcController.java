package com.keepreal.madagascar.angonoka.grpcController;

import com.keepreal.madagascar.angonoka.CancelFollowRequest;
import com.keepreal.madagascar.angonoka.CancelFollowResponse;
import com.keepreal.madagascar.angonoka.CreateSuperFollowSubscriptionRequest;
import com.keepreal.madagascar.angonoka.CreateSuperFollowSubscriptionResponse;
import com.keepreal.madagascar.angonoka.FollowRequest;
import com.keepreal.madagascar.angonoka.FollowResponse;
import com.keepreal.madagascar.angonoka.FollowServiceGrpc;
import com.keepreal.madagascar.angonoka.RetrieveAllSuperFollowRequest;
import com.keepreal.madagascar.angonoka.RetrieveAllSuperFollowResponse;
import com.keepreal.madagascar.angonoka.RetrieveSuperFollowRequest;
import com.keepreal.madagascar.angonoka.RetrieveSuperFollowResponse;
import com.keepreal.madagascar.angonoka.RetrieveWeiboProfileRequest;
import com.keepreal.madagascar.angonoka.WeiboProfileMessage;
import com.keepreal.madagascar.angonoka.WeiboProfileResponse;
import com.keepreal.madagascar.angonoka.model.SuperFollow;
import com.keepreal.madagascar.angonoka.service.FollowExecutorSelector;
import com.keepreal.madagascar.angonoka.service.FollowService;
import com.keepreal.madagascar.angonoka.service.impl.DefaultFollowExecutorSelectorImpl;
import com.keepreal.madagascar.angonoka.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.util.List;

/**
 * Represents the follow GRpc controller.
 */
@Slf4j
@GRpcService
public class FollowGRpcController extends FollowServiceGrpc.FollowServiceImplBase {

    private final FollowService followService;
    private final FollowExecutorSelector followExecutorSelector;

    /**
     * Constructs the follow grpc controller
     *
     * @param followService {@link FollowService}
     * @param defaultFollowExecutorSelector {@link DefaultFollowExecutorSelectorImpl}
     */
    public FollowGRpcController(FollowService followService,
                                DefaultFollowExecutorSelectorImpl defaultFollowExecutorSelector) {
        this.followService = followService;
        this.followExecutorSelector = defaultFollowExecutorSelector;
    }

    /**
     * 根据昵称获取微博信息
     *
     * @param request {@link RetrieveWeiboProfileRequest}
     * @param responseObserver {@link WeiboProfileResponse}
     */
    @Override
    public void retrieveWeiboProfile(RetrieveWeiboProfileRequest request, StreamObserver<WeiboProfileResponse> responseObserver) {
        WeiboProfileResponse response = this.followService.retrieveWeiboProfileByName(request.getName());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 创建超级关注
     *
     * @param request {@link FollowRequest}
     * @param responseObserver {@link FollowResponse}
     */
    @Override
    public void followSocialPlatform(FollowRequest request, StreamObserver<FollowResponse> responseObserver) {
        FollowResponse response = this.followExecutorSelector.select(request.getFollowType()).follow(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 取消超级关注
     *
     * @param request {@link CancelFollowRequest}
     * @param responseObserver {@link CancelFollowResponse}
     */
    @Override
    public void cancelFollowSocialPlatform(CancelFollowRequest request, StreamObserver<CancelFollowResponse> responseObserver) {
        CancelFollowResponse response = this.followExecutorSelector.select(request.getFollowType()).cancelFollow(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveSuperFollowMessage(RetrieveSuperFollowRequest request, StreamObserver<RetrieveSuperFollowResponse> responseObserver) {
        SuperFollow superFollow = this.followService.retrieveSuperFollowMessageByCode(request.getCode());
        responseObserver.onNext(RetrieveSuperFollowResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setSuperFollowMessage(this.followService.getSuperFollowMessage(superFollow))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveAllSuperFollowMessage(RetrieveAllSuperFollowRequest request, StreamObserver<RetrieveAllSuperFollowResponse> responseObserver) {
        List<SuperFollow> superFollowList = this.followService.retrieveAllSuperFollowMessageByCode(request.getHostId());
        responseObserver.onNext(this.followService.valueOf(superFollowList));
        responseObserver.onCompleted();
    }

    @Override
    public void createSuperFollowSubscription(CreateSuperFollowSubscriptionRequest request, StreamObserver<CreateSuperFollowSubscriptionResponse> responseObserver) {
        String hostId = request.getHostId();
        String openId = request.getOpenId();
        String superFollowId = request.getSuperFollowId();
        this.followService.createSuperFollowSubscription(hostId, openId, superFollowId);
        responseObserver.onNext(CreateSuperFollowSubscriptionResponse.newBuilder().setStatus(CommonStatusUtils.getSuccStatus()).build());
        responseObserver.onCompleted();
    }

}
