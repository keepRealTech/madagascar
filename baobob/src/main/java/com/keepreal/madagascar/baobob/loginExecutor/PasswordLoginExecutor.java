package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.config.AdminLoginConfiguration;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;

/**
 * Represents a login executor working with user combination.
 */
@Slf4j
public class PasswordLoginExecutor implements LoginExecutor {

    private static final String DUMMY_USER_ID = "0";

    private final LocalTokenGranter tokenGranter;
    private final UserService userService;
    private final GrpcResponseUtils grpcResponseUtils;
    private final BCryptPasswordEncoder encoder;
    private final AdminLoginConfiguration adminLoginConfiguration;

    /**
     * Constructs the executor.
     *
     * @param userService  User service.
     * @param tokenGranter Token granter.
     * @param adminLoginConfiguration   {@link AdminLoginConfiguration}
     */
    public PasswordLoginExecutor(UserService userService,
                                 LocalTokenGranter tokenGranter,
                                 AdminLoginConfiguration adminLoginConfiguration) {
        this.tokenGranter = tokenGranter;
        this.userService = userService;
        this.grpcResponseUtils = new GrpcResponseUtils();
        this.encoder = new BCryptPasswordEncoder();
        this.adminLoginConfiguration = adminLoginConfiguration;
    }

    /**
     * Overrides the login logic.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        if (!loginRequest.hasPasswordPayload()) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }

        if (loginRequest.getPasswordPayload().getAdmin() && !adminLoginConfiguration.getEnableAdminLogin()) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }

        String username = loginRequest.getPasswordPayload().getCode() + "-" + loginRequest.getPasswordPayload().getUsername();
        String password = loginRequest.getPasswordPayload().getPassword();

        return Mono.just(loginRequest.getPasswordPayload().getAdmin())
                .flatMap(bool -> {
                    if (bool) {
                        return this.loginWithAdminCombination(username, password);
                    }
                    return this.loginWithUserCombination(username, password);
                })
                .map(this.tokenGranter::grant)
                .onErrorReturn(throwable -> throwable instanceof KeepRealBusinessException
                                && ((KeepRealBusinessException) throwable).getErrorCode() == ErrorCode.REQUEST_GRPC_LOGIN_FROZEN,
                        this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN))
                .onErrorReturn(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
    }

    /**
     * Logs in with user combinations.¶
     *
     * @param username User name.
     * @param password Password.
     * @return {@link UserMessage}.
     */
    @SuppressWarnings("unchecked")
    private Mono<UserMessage> loginWithUserCombination(String username, String password) {
        return this.userService.retrieveUserByUsernameMono(username)
                .map(userMessage -> {
                    if (userMessage.getLocked()) {
                        throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN);
                    }
                    return userMessage;
                })
                .filter(userMessage -> this.encoder.matches(password, userMessage.getPassword()))
                .switchIfEmpty(Mono.error(new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID)));
    }

    /**
     * Logs in with admin combinations.¶
     *
     * @param username User name.
     * @param password Admin Password.
     * @return {@link UserMessage}.
     */
    private Mono<UserMessage> loginWithAdminCombination(String username, String password) {
        return this.userService.retrieveUserByUsernameMono(username)
                .map(userMessage -> {
                    if (userMessage.getLocked()) {
                        throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN);
                    }
                    return userMessage;
                })
                .filter(userMessage -> this.encoder.matches(password, userMessage.getAdminPassword()))
                .switchIfEmpty(Mono.error(new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID)));
    }

}