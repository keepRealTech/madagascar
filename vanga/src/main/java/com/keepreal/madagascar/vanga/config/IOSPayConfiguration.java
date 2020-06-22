package com.keepreal.madagascar.vanga.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Represents the rest template configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "ios-pay", ignoreUnknownFields = false)
@Data
public class IOSPayConfiguration {

    private String verifyUrl;

    /**
     * TODO: config connection pool, timeout
     *
     * @return  {@link RestTemplate}.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
