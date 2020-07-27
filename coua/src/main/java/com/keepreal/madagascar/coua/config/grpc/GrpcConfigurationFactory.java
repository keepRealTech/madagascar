package com.keepreal.madagascar.coua.config.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc configurations.
 */
@Configuration
public class GrpcConfigurationFactory {

    /**
     * Represents configurations for fossa.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "fossaConfiguration")
    @ConfigurationProperties(prefix = "grpc.fossa", ignoreUnknownFields = false)
    public GrpcConfiguration fossaConfiguration() {
        return new GrpcConfiguration();
    }

    /**
     * Represents configurations for vanga.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "vangaConfiguration")
    @ConfigurationProperties(prefix = "grpc.vanga", ignoreUnknownFields = false)
    public GrpcConfiguration vangaConfiguration() {
        return new GrpcConfiguration();
    }

    /**
     * Represents configurations for asity.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "asityConfiguration")
    @ConfigurationProperties(prefix = "grpc.asity", ignoreUnknownFields = false)
    public GrpcConfiguration asityConfiguration() {
        return new GrpcConfiguration();
    }

}