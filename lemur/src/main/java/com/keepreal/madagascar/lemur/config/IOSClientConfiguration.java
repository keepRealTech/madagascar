package com.keepreal.madagascar.lemur.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import swagger.model.ConfigurationDTO;
import swagger.model.UpdateInfoDTO;

import java.util.Map;

/**
 * Represents the android client configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "client.ios", ignoreUnknownFields = false)
@Data
public class IOSClientConfiguration {

    private Map<Integer, ConfigurationDTO> versionInfoMap;
    private Map<Integer, UpdateInfoDTO> updateInfoMap;

    private String membershipAuditUrl;
    private String membershipPayUrl;
    private String sponsorAuditUrl;
    private String sponsorPayUrl;

}
