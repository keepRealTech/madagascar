package com.keepreal.madagascar.angonoka.config.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc configurations.
 */
@Configuration
public class GrpcConfigurationFactory {

    /**
     * Represents configurations for hawksbill.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "hawksbillConfiguration")
    @ConfigurationProperties(prefix = "grpc.hawksbill", ignoreUnknownFields = false)
    public GrpcConfiguration hawksbillConfiguration() {
        return new GrpcConfiguration();
    }

}