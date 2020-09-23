package com.keepreal.madagascar.workflow.update_payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "login")
@Configuration
@Data
public class LoginConfiguration {

    private String username;
    private String password;
    private String host;
    private String path;
}
