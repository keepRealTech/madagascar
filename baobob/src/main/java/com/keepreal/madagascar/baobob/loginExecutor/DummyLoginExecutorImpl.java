package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.error.ErrorCode;
import reactor.core.publisher.Mono;

/**
 * Represents a dummy implementation for login executor.
 */
public class DummyLoginExecutorImpl implements LoginExecutor {

    private final GrpcResponseUtils grpcResponseUtils = new GrpcResponseUtils();

    /**
     * Overrides the login logic.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.GRPC_NOT_IMPLEMENTED_FUNCTION_ERROR));
    }

}
