package com.keepreal.madagascar.baobob.config.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc configurations.
 */
@Configuration
public class GrpcConfigurationFactory {

    /**
     * Represents configurations for coua.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "couaConfiguration")
    @ConfigurationProperties(prefix = "grpc.coua", ignoreUnknownFields = false)
    public GrpcConfiguration couaConfiguration() {
        return new GrpcConfiguration();
    }

    /**
     * Represents configurations for indri.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "indriConfiguration")
    @ConfigurationProperties(prefix = "grpc.indri", ignoreUnknownFields = false)
    public GrpcConfiguration indriConfiguration() {
        return new GrpcConfiguration();
    }

}