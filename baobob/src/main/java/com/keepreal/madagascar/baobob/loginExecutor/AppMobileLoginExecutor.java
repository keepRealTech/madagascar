package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.UserState;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;


import static com.keepreal.madagascar.baobob.loginExecutor.WebMobileLoginExecutor.MOBILE_PHONE_OTP;


/**
 * Represents a login executor working with mobile phone (app / official website).
 */
@Slf4j
public class AppMobileLoginExecutor implements LoginExecutor {

    private final LocalTokenGranter tokenGranter;
    private final UserService userService;
    private final GrpcResponseUtils grpcResponseUtils;
    private final RedissonReactiveClient redissonReactiveClient;

    public AppMobileLoginExecutor(UserService userService,
                                  LocalTokenGranter tokenGranter,
                                  RedissonClient redissonClient) {
        this.userService = userService;
        this.tokenGranter = tokenGranter;
        this.redissonReactiveClient = Redisson.createReactive(redissonClient.getConfig());
        this.grpcResponseUtils = new GrpcResponseUtils();
    }

    /**
     * Overrides the login logic.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return {@link LoginResponse}
     */
    @Override
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        if (!loginRequest.hasAppMobilePayload()) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }

        String mobile = loginRequest.getAppMobilePayload().getCode() + "-" + loginRequest.getAppMobilePayload().getMobile();
        int otp = loginRequest.getAppMobilePayload().getOtp();
        RBucketReactive<Integer> bucket = this.redissonReactiveClient.getBucket(MOBILE_PHONE_OTP + mobile);

        return bucket.isExists()
                .filter(Boolean.TRUE::equals)
                .flatMap(exist -> bucket.get())
                .filter(redisOtp -> otp == redisOtp)
                .flatMap(redisOtp -> bucket.delete()
                            .then(this.retrieveOrCreateUserByMobile(mobile))
                            .map(this.tokenGranter::grant)
                            .onErrorReturn(throwable -> throwable instanceof KeepRealBusinessException
                                            && ((KeepRealBusinessException) throwable).getErrorCode() == ErrorCode.REQUEST_GRPC_LOGIN_FROZEN,
                                    this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN))
                            .onErrorReturn(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID)))
                .switchIfEmpty(Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_USER_MOBILE_OTP_NOT_MATCH)));
    }

    /**
     * 根据手机号获取或者创建 APP/官网 用户信息
     *
     * @param mobile 手机号
     * @return {@link UserMessage}
     */
    private Mono<UserMessage> retrieveOrCreateUserByMobile(String mobile) {
        assert !StringUtils.isEmpty(mobile);
        return this.userService.retrieveUserByMobileMono(mobile)
                .map(userMessage -> {
                    if (userMessage.getLocked()) {
                        throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN);
                    }
                    if (userMessage.getState() == UserState.USER_H5_MOBILE_VALUE) {
                        this.userService.updateUserStateMono(userMessage.getId(), UserState.USER_APP_MOBILE_VALUE).subscribe();
                    }
                    return userMessage;
                })
                .switchIfEmpty(this.userService.createUserByMobileAndStateMono(mobile, UserState.USER_APP_MOBILE_VALUE));
    }

}
