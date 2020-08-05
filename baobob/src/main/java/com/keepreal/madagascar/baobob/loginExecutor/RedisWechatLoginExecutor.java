package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatUserInfo;
import com.keepreal.madagascar.baobob.service.ImageService;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Represents the redis wechat login executor.
 */
@Slf4j
public class RedisWechatLoginExecutor implements LoginExecutor {

    private final LocalTokenGranter tokenGranter;
    private final UserService userService;
    private final ImageService imageService;
    private final RedissonClient redissonClient;
    private final GrpcResponseUtils grpcResponseUtils;

    /**
     * Constructs the redis wechat login executor.
     *
     * @param userService    {@link UserService}.
     * @param tokenGranter   {@link LocalTokenGranter}
     * @param imageService   {@link ImageService}.
     * @param redissonClient {@link RedissonClient}.
     */
    public RedisWechatLoginExecutor(UserService userService,
                                    LocalTokenGranter tokenGranter,
                                    ImageService imageService,
                                    RedissonClient redissonClient) {
        this.tokenGranter = tokenGranter;
        this.userService = userService;
        this.imageService = imageService;
        this.redissonClient = redissonClient;
        this.grpcResponseUtils = new GrpcResponseUtils();
    }

    /**
     * Overrides the login logic.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        if (!loginRequest.hasMpScenePayload()) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }

        RBucket<WechatUserInfo> bucket = this.redissonClient.getBucket(loginRequest.getMpScenePayload().getSceneId());
        WechatUserInfo wechatUserInfo = bucket.get();
        if (Objects.nonNull(wechatUserInfo)) {
            return this.retrieveOrCreateUserByUnionId(wechatUserInfo)
                    .map(usermessage -> this.tokenGranter.grant(usermessage, wechatUserInfo.getOpenId()))
                    .doOnError(error -> log.error(error.toString()))
                    .onErrorReturn(throwable -> throwable instanceof KeepRealBusinessException
                                    && ((KeepRealBusinessException) throwable).getErrorCode() == ErrorCode.REQUEST_GRPC_LOGIN_FROZEN,
                            this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN))
                    .onErrorReturn(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }
        return Mono.just(LoginResponse.newBuilder().setStatus(this.grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC)).build());
    }

    /**
     * Retrieves a user by union id, create a new one if not exists.
     *
     * @param wechatUserInfo {@link WechatUserInfo}.
     * @return {@link UserMessage}.
     */
    private Mono<UserMessage> retrieveOrCreateUserByUnionId(WechatUserInfo wechatUserInfo) {
        assert !StringUtils.isEmpty(wechatUserInfo.getUnionId());
        return this.userService.retrieveUserByUnionIdMono(wechatUserInfo.getUnionId())
                .map(userMessage -> {
                    if (userMessage.getLocked()) {
                        throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN);
                    }
                    return userMessage;
                })
                .switchIfEmpty(this.createNewUserFromRedisWechatInfo(wechatUserInfo));
    }

    /**
     * Creates a new user from wechat user info.
     *
     * @param wechatUserInfo {@link WechatUserInfo}.
     * @return {@link UserMessage}.
     */
    private Mono<UserMessage> createNewUserFromRedisWechatInfo(WechatUserInfo wechatUserInfo) {
        return this.migrateImage(wechatUserInfo)
                .flatMap(this.userService::createUserByWechatUserInfoMono);
    }

    /**
     * Migrates the image from wechat to oss.
     *
     * @param wechatUserInfo {@link WechatUserInfo}.
     * @return {@link WechatUserInfo}.
     */
    private Mono<WechatUserInfo> migrateImage(WechatUserInfo wechatUserInfo) {
        if (StringUtils.isEmpty(wechatUserInfo.getPortraitImageUri())) {
            return Mono.just(wechatUserInfo);
        }

        return Mono.just(wechatUserInfo.getPortraitImageUri())
                .flatMap(this.imageService::migrateSingleImage)
                .map(uri -> {
                    wechatUserInfo.setPortraitImageUri(uri);
                    return wechatUserInfo;
                });
    }

}
