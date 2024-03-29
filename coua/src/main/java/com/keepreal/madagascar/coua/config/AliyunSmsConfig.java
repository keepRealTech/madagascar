package com.keepreal.madagascar.coua.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aliyun.sms")
@Data
public class AliyunSmsConfig {

    private String accessKey;
    private String accessSecret;
    private String templateId;
    private String internationalTemplateId;

}
