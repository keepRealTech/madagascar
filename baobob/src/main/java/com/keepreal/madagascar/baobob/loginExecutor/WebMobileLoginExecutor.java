package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.baobob.util.ReactiveAutoRedisLock;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.UserState;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;


/**
 * Represents a login executor working with mobile phone.
 */
@Slf4j
public class WebMobileLoginExecutor implements LoginExecutor{

    public static final String MOBILE_PHONE_OTP = "otp_";
    private final LocalTokenGranter tokenGranter;
    private final UserService userService;
    private final GrpcResponseUtils grpcResponseUtils;
    private final RedissonReactiveClient redissonReactiveClient;

    public WebMobileLoginExecutor(UserService userService,
                                  LocalTokenGranter tokenGranter,
                                  RedissonReactiveClient redissonClient) {
        this.userService = userService;
        this.tokenGranter = tokenGranter;
        this.redissonReactiveClient = redissonClient;
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
        if (!loginRequest.hasWebMobilePayload()) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }

        String mobile = loginRequest.getWebMobilePayload().getCode() + "-" + loginRequest.getWebMobilePayload().getMobile();
        int otp = loginRequest.getWebMobilePayload().getOtp();

        try (ReactiveAutoRedisLock ignored = new ReactiveAutoRedisLock(this.redissonReactiveClient, "try-get-otp-" + mobile)) {
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
    }

    /**
     * 根据手机号获取或者创建 H5用户信息
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
                    return userMessage;
                })
                .switchIfEmpty(this.userService.createUserByMobileAndStateMono(mobile, UserState.USER_H5_MOBILE_VALUE));
    }

}
