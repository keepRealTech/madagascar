package com.keepreal.madagascar.baobob.config;

import com.keepreal.madagascar.baobob.service.BaobobUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.Collections;

/**
 * Represents the authorization server configurations.
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private final JwtTokenConfiguration jwtTokenConfiguration;
    private final BaobobUserDetailsService userDetailsService;

    /**
     * Constructs the authorization server configuration.
     *
     * @param jwtTokenConfiguration Jwt token configuration.
     * @param userDetailsService    {@link BaobobUserDetailsService}.
     */
    public AuthorizationServerConfiguration(
            JwtTokenConfiguration jwtTokenConfiguration,
            BaobobUserDetailsService userDetailsService) {
        this.jwtTokenConfiguration = jwtTokenConfiguration;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Configures the client details service for authorization.
     *
     * @param clients Client.
     * @throws Exception Exceptions.
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("lemur")
                .authorizedGrantTypes("local", "refresh_token")
                .secret(this.jwtTokenConfiguration.getClientSecret())
                .scopes("all")
                .accessTokenValiditySeconds(this.jwtTokenConfiguration.getAccessTokenValidityInSeconds())
                .refreshTokenValiditySeconds(this.jwtTokenConfiguration.getRefreshTokenValidityInSeconds());
    }

    /**
     * Configures the authorization server security.
     *
     * @param security Security.
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients();
    }

    /**
     * Configures the token store.
     *
     * @return Retail token store.
     */
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(this.accessTokenConverter());
    }

    /**
     * Represents the jwt access token converter used for constructing a token.
     *
     * @return Jwt access token converter.
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(this.jwtTokenConfiguration.getTokenSecret());
        return converter;
    }

    /**
     * Configures the authorization server endpoints. Circuit breaker might be configured here.
     *
     * @param endpoints Endpoints.
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Collections.singletonList(this.accessTokenConverter()));

        endpoints
                .userDetailsService(this.userDetailsService)
                .tokenEnhancer(tokenEnhancerChain)
                .tokenStore(this.tokenStore())
                .allowedTokenEndpointRequestMethods(HttpMethod.GET);
    }

    /**
     * Represents the default password encoder. This is required by Spring 5 and will be used by the client secrets.
     *
     * @return Password encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

}