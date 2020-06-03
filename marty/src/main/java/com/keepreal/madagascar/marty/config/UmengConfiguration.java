package com.keepreal.madagascar.marty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-06-03
 **/

@Configuration
@ConfigurationProperties(prefix = "umeng")
@Data
public class UmengConfiguration {
    private String appKey;
    private String appMasterSecret;
    private String url;
}
