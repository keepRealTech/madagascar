package com.keepreal.madagascar.mantella.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the distribution configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "distributor", ignoreUnknownFields = false)
@Data
public class DistributorConfiguration {
}
