package com.keepreal.madagascar.baobob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the wechat oauth login configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "wechat.oauth", ignoreUnknownFields = false)
@Data
public class OauthWechatLoginConfiguration {

    String appId;
    String appSecret;
    String host;

}
