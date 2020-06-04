package com.keepreal.madagascar.marty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the umeng configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "umeng")
@Data
public class UmengConfiguration {
    private String androidAppKey;
    private String androidAppMasterSecret;
    private String url;
}
