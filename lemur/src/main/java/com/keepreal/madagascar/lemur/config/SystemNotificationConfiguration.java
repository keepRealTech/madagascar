package com.keepreal.madagascar.lemur.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the system notification configurations.
 */
@Configuration
@ConfigurationProperties(prefix="system-notification")
@Data
public class SystemNotificationConfiguration {

    private String name;
    private String portraitImageUri;
    private String content;

}
