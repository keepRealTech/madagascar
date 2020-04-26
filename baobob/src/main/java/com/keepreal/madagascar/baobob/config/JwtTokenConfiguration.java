package com.keepreal.madagascar.baobob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the jwt toekn configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt", ignoreUnknownFields = false)
@Data
public class JwtTokenConfiguration {

    private String tokenSecret;
    private Integer accessTokenValidityInSeconds;
    private Integer refreshTokenValidityInSeconds;
    private String clientSecret;

}
