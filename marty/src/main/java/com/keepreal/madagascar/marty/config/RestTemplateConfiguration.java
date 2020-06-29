package com.keepreal.madagascar.marty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Represents the rest template configurations.
 */
@Configuration
public class RestTemplateConfiguration {

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
