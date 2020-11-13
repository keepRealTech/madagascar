package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
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
import com.keepreal.madagascar.angonoka.SuperFollowMessage;
import com.keepreal.madagascar.angonoka.WeiboProfileMessage;
import com.keepreal.madagascar.angonoka.WeiboProfileResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.hoopoe.BannerResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Represents the follow service.
 */
@Service
@Slf4j
public class FollowService {

    private final Channel channel;

    /**
     * Constructs the follow service.
     *
     * @param channel GRpc managed channel connection to service Angonoka.
     */
    public FollowService(@Qualifier("angonokaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * 根据暗号获取超级关注信息
     *
     * @param code 暗号
     * @return {@link SuperFollowMessage}
     */
    public SuperFollowMessage retrieveSuperFollowByCode(String code) {
        FollowServiceGrpc.FollowServiceBlockingStub stub = FollowServiceGrpc.newBlockingStub(this.channel);
        RetrieveSuperFollowRequest.Builder builder = RetrieveSuperFollowRequest.newBuilder();

        builder.setCode(code);

        RetrieveSuperFollowResponse response;

        try {
            response = stub.retrieveSuperFollowMessage(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve super follow message by code returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getSuperFollowMessage();
    }

    /**
     * 创建超级关注
     *
     * @param request {@link FollowRequest}
     * @return {@link SuperFollowMessage}
     */
    public SuperFollowMessage followSocialPlatform(FollowRequest request) {
        FollowServiceGrpc.FollowServiceBlockingStub stub = FollowServiceGrpc.newBlockingStub(this.channel);

        FollowResponse response;

        try {
            response = stub.followSocialPlatform(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "follow social platform returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getSuperFollowMessage();
    }

    /**
     * 删除超级关注超级关注
     *
     * @param request {@link CancelFollowRequest}
     */
    public void cancelFollowSocialPlatform(CancelFollowRequest request) {
        FollowServiceGrpc.FollowServiceBlockingStub stub = FollowServiceGrpc.newBlockingStub(this.channel);

        CancelFollowResponse response;

        try {
            response = stub.cancelFollowSocialPlatform(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "follow social platform returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }

    /**
     * 根据昵称获取微博信息
     *
     * @param name 微博昵称
     * @return {@link WeiboProfileMessage}
     */
    public WeiboProfileMessage retrieveWeiboProfileByNickName(String name) {
        FollowServiceGrpc.FollowServiceBlockingStub stub = FollowServiceGrpc.newBlockingStub(this.channel);
        RetrieveWeiboProfileRequest request = RetrieveWeiboProfileRequest.newBuilder().setName(name).build();
        WeiboProfileResponse response;

        try {
            response = stub.retrieveWeiboProfile(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve weibo profile by name returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getWeiboMessage();
    }

    /**
     * 创建超级关注订阅
     *
     * @param openId mp wechat open id
     * @param hostId host id
     * @param superFollowId super follow id
     */
    public void createSuperFollowSubscription(String openId, String hostId, String superFollowId) {
        FollowServiceGrpc.FollowServiceBlockingStub stub = FollowServiceGrpc.newBlockingStub(this.channel);
        CreateSuperFollowSubscriptionRequest request = CreateSuperFollowSubscriptionRequest.newBuilder()
                .setOpenId(openId)
                .setHostId(hostId)
                .setSuperFollowId(superFollowId)
                .build();
        CreateSuperFollowSubscriptionResponse response;
        try {
            response = stub.createSuperFollowSubscription(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "create super follow subscription returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }

    /**
     * 获取超级关注BOT信息
     *
     * @param hostId host id
     * @return {@link RetrieveAllSuperFollowResponse}
     */
    public RetrieveAllSuperFollowResponse retrieveAllSuperFollowBotByHostId(String hostId) {
        FollowServiceGrpc.FollowServiceBlockingStub stub = FollowServiceGrpc.newBlockingStub(this.channel);
        RetrieveAllSuperFollowRequest request = RetrieveAllSuperFollowRequest.newBuilder().setHostId(hostId).build();
        RetrieveAllSuperFollowResponse response;
        try {
            response = stub.retrieveAllSuperFollowMessage(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve all super follow returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

}
