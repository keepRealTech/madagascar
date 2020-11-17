package com.keepreal.madagascar.hawksbill.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat.mp", ignoreUnknownFields = false)
@Data
public class MpWechatConfiguration {

    String appId;
    String appSecret;
    String serverToken;
    String expirationInSec;
    String templateId;
    String templateColor;

}
