package com.keepreal.madagascar.common.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workflow")
@Data
public class WorkflowConfiguration {

    private String type;

}
