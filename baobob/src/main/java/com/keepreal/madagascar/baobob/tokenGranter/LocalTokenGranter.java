package com.keepreal.madagascar.baobob.tokenGranter;

import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a local token granter.
 */
public class LocalTokenGranter extends AbstractTokenGranter {

    public static final String GRANT_TYPE = "local";
    private static final String USER_PARAM_NAME = "id";
    private final GrpcResponseUtils grpcResponseUtils;

    /**
     * Constructs the local token granter.
     *
     * @param tokenServices        Token service.
     * @param clientDetailsService Client details service.
     * @param requestFactory       Request factory.
     */
    public LocalTokenGranter(AuthorizationServerTokenServices tokenServices,
                             ClientDetailsService clientDetailsService,
                             OAuth2RequestFactory requestFactory) {
        super(tokenServices, clientDetailsService, requestFactory, LocalTokenGranter.GRANT_TYPE);
        this.grpcResponseUtils = new GrpcResponseUtils();
    }

    /**
     * Grants a token for given user message.
     *
     * @param userMessage {@link UserMessage}.
     * @return {@link LoginResponse}.
     */
    public LoginResponse grant(UserMessage userMessage) {
        OAuth2AccessToken token = this.grant(LocalTokenGranter.GRANT_TYPE, this.buildTokenRequest(userMessage));
        return LoginResponse.newBuilder()
                .setStatus(this.grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setToken(token.getValue())
                .setRefreshToken(token.getRefreshToken().getValue())
                .setUserId(userMessage.getId())
                .build();
    }

    /**
     * Defines local grant type process.
     *
     * @param client       Client.
     * @param tokenRequest Token request.
     * @return OAuth2 authentication.
     */
    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> params = new LinkedHashMap<>(tokenRequest.getRequestParameters());

        if (!params.containsKey(LocalTokenGranter.USER_PARAM_NAME)) {
            throw new InvalidGrantException("Invalid parameter.");
        }

        String userId = params.get(LocalTokenGranter.USER_PARAM_NAME);

        AbstractAuthenticationToken userAuth = new UsernamePasswordAuthenticationToken(userId,
                "N/A",
                Collections.singletonList(new SimpleGrantedAuthority(String.format("user_%s", userId))));
        userAuth.setDetails(params);

        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);

        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }

    /**
     * Builds a token request.
     *
     * @param userMessage {@link UserMessage}.
     * @return {@link TokenRequest}.
     */
    private TokenRequest buildTokenRequest(UserMessage userMessage) {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put(LocalTokenGranter.USER_PARAM_NAME, userMessage.getId());

        return new TokenRequest(requestParameters,
                "lemur",
                Collections.singleton("all"),
                LocalTokenGranter.GRANT_TYPE);
    }

}
