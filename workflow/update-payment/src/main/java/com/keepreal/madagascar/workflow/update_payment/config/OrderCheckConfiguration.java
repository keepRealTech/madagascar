package com.keepreal.madagascar.workflow.update_payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "order")
@Data
public class OrderCheckConfiguration {

    private String host;
    private String pathTemplate;
}
