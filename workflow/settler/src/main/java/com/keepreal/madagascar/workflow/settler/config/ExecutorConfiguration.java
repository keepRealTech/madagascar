package com.keepreal.madagascar.workflow.settler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "executor")
@Data
public class ExecutorConfiguration {

    private Integer threads;

}
