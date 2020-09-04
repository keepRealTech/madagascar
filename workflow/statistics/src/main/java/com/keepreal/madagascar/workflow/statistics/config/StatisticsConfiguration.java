package com.keepreal.madagascar.workflow.statistics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the
 */
@Configuration
@ConfigurationProperties(prefix = "statistics")
@Data
public class StatisticsConfiguration {

    private Integer increIslanderThreshold;

}
