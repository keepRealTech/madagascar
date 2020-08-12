package com.keepreal.madagascar.fossa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the general configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "madagascar", ignoreUnknownFields = false)
@Data
public class GeneralConfiguration {

    private String shortCodeBase;

}
