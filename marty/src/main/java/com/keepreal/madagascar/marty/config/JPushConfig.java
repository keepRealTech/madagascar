package com.keepreal.madagascar.marty.config;

import cn.jpush.api.JPushClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;

@Configuration
@Data
public class JPushConfig {

    @Value("${jpush.key}")
    public String appKey;
    @Value("${jpush.secret}")
    public String appSecret;

    @Bean
    public JPushClient jPushClient() {
        return new JPushClient(appSecret, appKey);
    }

}
