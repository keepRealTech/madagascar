package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

/**
 * Represents a login executor working with user combination.
 */
public class PasswordLoginExecutor implements LoginExecutor {

    private static final String DUMMY_USER_ID = "0";

    private final LocalTokenGranter tokenGranter;
    private final UserService userService;
    private final GrpcResponseUtils grpcResponseUtils;
    private final BCryptPasswordEncoder encoder;

    /**
     * Constructs the executor.
     *
     * @param userService  User service.
     * @param tokenGranter Token granter.
     */
    public PasswordLoginExecutor(UserService userService,
                                 LocalTokenGranter tokenGranter) {
        this.tokenGranter = tokenGranter;
        this.userService = userService;
        this.grpcResponseUtils = new GrpcResponseUtils();
        this.encoder = new BCryptPasswordEncoder();
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

        return this.loginWithUserCombination(
                loginRequest.getPasswordPayload().getUsername(), loginRequest.getPasswordPayload().getPassword())
                .map(this.tokenGranter::grant)
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
                .handle((userMessage, sink) -> {
                    if (userMessage.getLocked()) {
                        sink.error(new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN));
                    }
                    sink.next(userMessage);
                })
                .map(object -> (UserMessage) object)
                .filter(userMessage -> this.encoder.matches(password, userMessage.getPassword()))
                .switchIfEmpty(Mono.error(new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID)));
    }

}