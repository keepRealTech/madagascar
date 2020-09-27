package com.keepreal.madagascar.baobob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "loginsetting", ignoreInvalidFields = false)
@Data
public class AdminLoginConfiguration {
    Boolean enableAdminLogin;
}
