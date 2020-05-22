package com.keepreal.madagascar.lemur.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the android client configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "client.android", ignoreUnknownFields = false)
@Data
public class AndroidClientConfiguration {

    private SetupInfo setup;

    @Data
    public static class SetupInfo {
        private Integer version;
        private String address;
    }

}