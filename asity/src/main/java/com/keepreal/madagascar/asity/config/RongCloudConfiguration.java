package com.keepreal.madagascar.asity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the rong cloud configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "rongcloud")
@Data
public class RongCloudConfiguration {

    private String appKey;
    private String appSecret;

}
