package com.keepreal.madagascar.vanga.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the ali pay configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "ali-pay", ignoreUnknownFields = false)
@Data
public class AlipayConfiguration {

    private String appId;
    private String merchantKey;
    private String callbackAddress;
    private String merchantCertPath;
    private String alipayCertPath;
    private String alipayRootPath;

}
