package com.keepreal.madagascar.workflow.statistics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Represents the lark configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "lark")
@Data
public class LarkConfiguration {

    private String webhook;

    /**
     * Represents the rest template for http call.
     *
     * @return {@link RestTemplate}.
     */
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
