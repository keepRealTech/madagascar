package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the refresh token login executor.
 */
public class RefreshLoginExecutor implements LoginExecutor {

    private final RefreshTokenGranter refreshTokenGranter;
    private final GrpcResponseUtils grpcResponseUtils;

    /**
     * Constructs the refresh login executor.
     *
     * @param refreshTokenGranter {@link RefreshTokenGranter}.
     */
    public RefreshLoginExecutor(RefreshTokenGranter refreshTokenGranter) {
        this.refreshTokenGranter = refreshTokenGranter;
        this.grpcResponseUtils = new GrpcResponseUtils();
    }

    /**
     * Implements the refresh token logic.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        if (!loginRequest.hasTokenRefreshPayload()) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }

        return Mono.just(loginRequest.getTokenRefreshPayload().getRefreshToken())
                .map(token -> {
                    Map<String, String> requestParameters = new HashMap<>();
                    requestParameters.put("refresh_token", token);

                    return new TokenRequest(requestParameters,
                            "lemur",
                            Collections.singleton("all"),
                            "refresh_token");
                })
                .map(request -> {
                    OAuth2AccessToken refresh_token = this.refreshTokenGranter.grant("refresh_token", request);
                    return LoginResponse.newBuilder()
                            .setStatus(this.grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                            .setToken(refresh_token.getValue())
                            .setRefreshToken(refresh_token.getRefreshToken().getValue())
                            .build();
                })
                .onErrorReturn(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_INVALID_REFRESH_TOKEN));
    }
}