package com.keepreal.madagascar.angonoka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the weibo business configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "weibo")
@Data
public class WeiboBusinessConfig {
    private String appKey;
    private String accessToken;
    private String subId;
}
