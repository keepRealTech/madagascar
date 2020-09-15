package com.keepreal.madagascar.workflow.updatePayment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "lark")
@Data
public class LarkConfiguration {

    private String webhook;
}
