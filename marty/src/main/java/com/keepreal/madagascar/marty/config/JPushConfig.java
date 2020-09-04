package com.keepreal.madagascar.marty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jpush")
@Data
public class JPushConfig {

    private String appKey;
    private String appSecret;
    private Boolean isProduction;

}
