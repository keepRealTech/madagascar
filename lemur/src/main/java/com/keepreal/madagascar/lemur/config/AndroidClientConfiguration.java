package com.keepreal.madagascar.lemur.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import swagger.model.UpdateInfoDTO;

import java.util.Map;

/**
 * Represents the android client configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "client.android", ignoreUnknownFields = false)
@Data
public class AndroidClientConfiguration {

    private SetupInfo setup;
    private Map<String, Boolean> androidChannelMap;
    private UpdateInfoDTO updateInfo;

    @Data
    public static class SetupInfo {
        private Integer version;
        private String address;
    }

}