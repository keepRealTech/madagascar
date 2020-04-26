package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import reactor.core.publisher.Mono;

/**
 * Represents the login executor interface.
 */
public interface LoginExecutor {

    /**
     * Logs in.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return Login response {@link LoginResponse}.
     */
    Mono<LoginResponse> login(LoginRequest loginRequest);

}
