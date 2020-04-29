package com.keepreal.madagascar.lemur.config.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc configurations.
 */
@Configuration
public class GrpcConfigurationFactory {

    /**
     * Represents configurations for baobob.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "baobobConfiguration")
    @ConfigurationProperties(prefix = "grpc.baobob", ignoreUnknownFields = false)
    public GrpcConfiguration baobobManagerConfiguration() {
        return new GrpcConfiguration();
    }

    /**
     * Represents configurations for coua.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "couaConfiguration")
    @ConfigurationProperties(prefix = "grpc.coua", ignoreUnknownFields = false)
    public GrpcConfiguration couaManagerConfiguration() {
        return new GrpcConfiguration();
    }

    /**
     * Represents configurations for fossa.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "fossaConfiguration")
    @ConfigurationProperties(prefix = "grpc.fossa", ignoreUnknownFields = false)
    public GrpcConfiguration fossaManagerConfiguration() {
        return new GrpcConfiguration();
    }

    /**
     * Represents configurations for indri.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "indriConfiguration")
    @ConfigurationProperties(prefix = "grpc.indri", ignoreUnknownFields = false)
    public GrpcConfiguration indriManagerConfiguration() {
        return new GrpcConfiguration();
    }

    /**
     * Represents configurations for tenrecs.
     *
     * @return Grpc configuration.
     */
    @Bean(name = "tenrecsConfiguration")
    @ConfigurationProperties(prefix = "grpc.tenrecs", ignoreUnknownFields = false)
    public GrpcConfiguration tenrecsConfiguration() {
        return new GrpcConfiguration();
    }

}