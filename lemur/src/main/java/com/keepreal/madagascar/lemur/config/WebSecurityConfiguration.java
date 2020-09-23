package com.keepreal.madagascar.lemur.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

/**
 * Represents the spring web security filter configurations.
 */
@Configuration
@EnableResourceServer
@EnableWebSecurity
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
                .antMatchers(
                        "/s/**",
                        "/api/v1/login**",
                        "/api/v1/mobile/otp**",
                        "/api/v1/login/generate**",
                        "/api/v1/login/polling**",
                        "/api/v1/refreshToken**",
                        "/api/v1/configs**",
                        "/api/v1/setupInfo/**",
                        "/api/v1/orders/wechat/callback**",
                        "/api/v1/orders/wechat/refund/callback**",
                        "/api/v1/events/wechatMp/callback**",
                        "/api/v1/orders/alipay/callback**",
                        "/api/v1/islands/{\\d+}/poster**",
                        "/api/v1/islands/{\\d+}/profile**",
                        "/api/v1/islands/{\\d+}/memberships**",
                        "/api/v1/memberships/{\\d+}**",
                        "/api/v1/membership/{\\d+}/skus**",
                        "/api/v1/islands/{\\d+}/support**",
                        "/api/v1/islands/{\\d+}/boxes**",
                        "/api/v1/islands/{\\d+}/reposts/generateCode**",
                        "/api/v1/islands/{\\d+}/feeds/snapshot**",
                        "/api/v1/islands/{\\d+}/feedgroups**"
                        ).permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
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
