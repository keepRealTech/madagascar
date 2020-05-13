package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import reactor.core.publisher.Mono;

/**
 * Represents a login executor working with user combination.
 */
public class DummyPasswordLoginExecutor implements LoginExecutor {

    private static final String DUMMY_USER_ID = "0";

    private final LocalTokenGranter tokenGranter;
    private final UserService userService;
    private final GrpcResponseUtils grpcResponseUtils;

    /**
     * Constructs the executor.
     *
     * @param userService  User service.
     * @param tokenGranter Token granter.
     */
    public DummyPasswordLoginExecutor(UserService userService,
                                      LocalTokenGranter tokenGranter) {
        this.tokenGranter = tokenGranter;
        this.userService = userService;
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
        if ("testuser".equals(username) && "testpass".equals(password)) {
            return this.userService.retrieveUserByIdMono("001");
        }
        switch (username) {
            case "user":
                return this.userService.retrieveUserByIdMono("0");
            case "user1":
                return this.userService.retrieveUserByIdMono("1");
            case "user2":
                return this.userService.retrieveUserByIdMono("2");
        }
        return Mono.error(new IllegalArgumentException());
    }

}
