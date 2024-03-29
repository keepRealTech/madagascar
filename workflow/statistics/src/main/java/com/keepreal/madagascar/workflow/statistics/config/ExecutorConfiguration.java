package com.keepreal.madagascar.workflow.statistics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the executor configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "executor")
@Data
public class ExecutorConfiguration {

    private Integer threads;

}
