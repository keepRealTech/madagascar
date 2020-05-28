package com.keepreal.madagascar.baobob.config;

import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.common.UserMessage;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * Represents the madagascar jwt token store.
 */
class MadagascarJwtTokenStore extends JwtTokenStore {

    private final UserService userService;

    /**
     * Creates a JwtTokenStore with this token enhancer (should be shared with the DefaultTokenServices if used).
     *
     * @param jwtTokenEnhancer Jwt token enhancer.
     * @param userService      User service.
     */
    MadagascarJwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer,
                            UserService userService) {
        super(jwtTokenEnhancer);
        this.userService = userService;
    }

    /**
     * Overrides the default read authentication method to verify the existence of system user.
     *
     * @param token Jwt token.
     * @return OAuth2 authentication.
     */
    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        OAuth2Authentication oauth2Authentication = super.readAuthentication(token);

        if (oauth2Authentication.isClientOnly()) {
            return oauth2Authentication;
        }

        UserMessage user = this.userService.retrieveUserByIdMono(oauth2Authentication.getPrincipal().toString())
                .onErrorResume(t -> Mono.empty())
                .block();

        if (null == user || user.getLocked()) {
            return null;
        }

        Authentication userAuthentication = new UsernamePasswordAuthenticationToken(
                user.getId(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(String.format("user_%s", user.getId()))));
        oauth2Authentication = new OAuth2Authentication(oauth2Authentication.getOAuth2Request(), userAuthentication);

        return oauth2Authentication;
    }

}
