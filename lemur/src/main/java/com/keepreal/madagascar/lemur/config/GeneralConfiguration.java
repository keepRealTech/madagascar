package com.keepreal.madagascar.lemur.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Represents the general configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "general", ignoreUnknownFields = false)
@Data
public class GeneralConfiguration {
    private List<String> officialIslandIdList;
}
