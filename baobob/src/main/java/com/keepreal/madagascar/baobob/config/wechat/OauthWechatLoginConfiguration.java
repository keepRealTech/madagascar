package com.keepreal.madagascar.baobob.config.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the wechat oauth login configurations.
 */
@Configuration
@Data
public class OauthWechatLoginConfiguration {

    String appId;
    String appSecret;
    String host;
    String serverToken;
    String expirationInSec;

}
