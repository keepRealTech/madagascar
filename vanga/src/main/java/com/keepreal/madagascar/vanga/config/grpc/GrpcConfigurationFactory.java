package com.keepreal.madagascar.vanga.config.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc configurations.
 */
@Configuration
public class GrpcConfigurationFactory {

    /**
     * Represents configurations for Fossa.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "fossaConfiguration")
    @ConfigurationProperties(prefix = "grpc.fossa", ignoreUnknownFields = false)
    public GrpcConfiguration fossaConfiguration() {
        return new GrpcConfiguration();
    }

}