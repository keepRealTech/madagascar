package com.keepreal.madagascar.coua.config.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc channel factory.
 */
@Configuration
public class GrpcChannelFactory {

    private final GrpcConfiguration fossaConfiguration;

    /**
     * Constructs the grpc channels factory.
     *
     * @param fossaConfiguration Fossa grpc configuration.
     */
    public GrpcChannelFactory(@Qualifier("fossaConfiguration") GrpcConfiguration fossaConfiguration) {
        this.fossaConfiguration = fossaConfiguration;
    }

    /**
     * Represents the fossa grpc channel.
     *
     * @return Fossa grpc channel.
     */
    @Bean(name = "fossaChannel")
    public ManagedChannel getFossaChannel() {
        return ManagedChannelBuilder
                .forAddress(this.fossaConfiguration.getHost(), this.fossaConfiguration.getPort())
                .usePlaintext()
                .build();
    }

}
