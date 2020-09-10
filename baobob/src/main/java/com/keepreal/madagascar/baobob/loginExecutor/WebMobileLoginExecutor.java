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
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
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
    private final RedissonClient redissonClient;

    public WebMobileLoginExecutor(UserService userService,
                                  LocalTokenGranter tokenGranter,
                                  RedissonClient redissonClient) {
        this.userService = userService;
        this.tokenGranter = tokenGranter;
        this.redissonClient = redissonClient;
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

        String mobile = loginRequest.getWebMobilePayload().getMobile();
        Integer otp = loginRequest.getWebMobilePayload().getOtp();

        if (!checkOtp(mobile, otp)) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_USER_MOBILE_OTP_NOT_MATCH));
        }

        return this.retrieveOrCreateUserByMobile(mobile)
                .map(this.tokenGranter::grant)
                .onErrorReturn(throwable -> throwable instanceof KeepRealBusinessException
                                && ((KeepRealBusinessException) throwable).getErrorCode() == ErrorCode.REQUEST_GRPC_LOGIN_FROZEN,
                        this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN))
                .onErrorReturn(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
    }

    /**
     * 判断验证码是否正确
     *
     * @param mobile 手机号
     * @param otp    验证码
     * @return 正确匹配返回true
     */
    private Boolean checkOtp(String mobile, Integer otp) {
        RBucket<Integer> redisOtp = this.redissonClient.getBucket(MOBILE_PHONE_OTP + mobile);
        if (redisOtp.isExists() && otp.equals(redisOtp.get())) {
            redisOtp.delete();
            return true;
        }
        return false;
    }

    /**
     * 根据手机号获取或者创建 H5用户信息
     *
     * @param mobile 手机号
     * @return {@link UserMessage}
     */
    private Mono<UserMessage> retrieveOrCreateUserByMobile(String mobile) {
        assert !StringUtils.isEmpty(mobile);
        return this.userService.retrieveUserByMobileAndStateMono(mobile, UserState.USER_WECHAT_VALUE)
                .switchIfEmpty(this.userService.retrieveUserByMobileAndStateMono(mobile, UserState.USER_APP_MOBILE_VALUE))
                .switchIfEmpty(this.userService.retrieveUserByMobileAndStateMono(mobile, UserState.USER_H5_MOBILE_VALUE))
                .map(userMessage -> {
                    if (userMessage.getLocked()) {
                        throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN);
                    }
                    return userMessage;
                })
                .switchIfEmpty(this.userService.createUserByMobileAndStateMono(mobile, UserState.USER_H5_MOBILE_VALUE));
    }

}
