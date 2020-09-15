package com.keepreal.madagascar.hoopoe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "activity")
@Data
public class ActivityConfiguration {

    private Boolean showLabel;
    private String  text;

}
