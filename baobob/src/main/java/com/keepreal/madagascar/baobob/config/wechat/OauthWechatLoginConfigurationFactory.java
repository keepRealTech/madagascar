package com.keepreal.madagascar.baobob.config.wechat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the oauth wechat configurations.
 */
@Configuration
public class OauthWechatLoginConfigurationFactory {

    /**
     * Represents configurations for wechat app.
     *
     * @return Wechat oauth configuration.
     */
    @Bean(name = "wechatAppConfiguration")
    @ConfigurationProperties(prefix = "wechat.oauth", ignoreUnknownFields = false)
    public OauthWechatLoginConfiguration wechatAppConfiguration() {
        return new OauthWechatLoginConfiguration();
    }

    /**
     * Represents configurations for wechat mp.
     *
     * @return Wechat mp oauth configuration.
     */
    @Bean(name = "wechatMpConfiguration")
    @ConfigurationProperties(prefix = "wechat.mp", ignoreUnknownFields = false)
    public OauthWechatLoginConfiguration wechatMpConfiguration() {
        return new OauthWechatLoginConfiguration();
    }

}