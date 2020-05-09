package com.keepreal.madagascar.lemur.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;

/**
 * Represents the spring web security filter configurations.
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class WebSecurityConfiguration extends ResourceServerConfigurerAdapter {

    /**
     * Configures the http security.
     *
     * @param httpSecurity Http security.
     * @throws Exception Exception.
     */
    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors().and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/api/v1/login**", "/api/v1/refreshToken**", "/api/v1/configs**").permitAll()
                .anyRequest().authenticated();

        httpSecurity.headers().cacheControl();
    }

    /**
     * Overrides the error handling logic.
     *
     * @param config {@link ResourceServerConfigurer}.
     */
    @Override
    public void configure(final ResourceServerSecurityConfigurer config) {
        OAuth2AccessDeniedHandler auth2AccessDeniedHandler = new OAuth2AccessDeniedHandler();
        auth2AccessDeniedHandler.setExceptionTranslator(new OAuthExceptionHandler());
        OAuth2AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
        authenticationEntryPoint.setExceptionTranslator(new OAuthExceptionHandler());

        config.accessDeniedHandler(auth2AccessDeniedHandler)
                .authenticationEntryPoint(authenticationEntryPoint);
    }

}
