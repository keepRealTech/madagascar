package com.keepreal.madagascar.lemur.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the geo ip database file.
 */
@Configuration
@ConfigurationProperties(prefix = "maxmind.geo")
@Data
public class MaxMindGeoConfiguration {

    private String databasePath;

}
