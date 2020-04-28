package com.keepreal.madagascar.baobob.config.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc channel factory.
 */
@Configuration
public class GrpcChannelFactory {

    private final GrpcConfiguration couaConfiguration;

    /**
     * Constructs the grpc channels factory.
     *
     * @param couaConfiguration Coua grpc configuration.
     */
    public GrpcChannelFactory(@Qualifier("couaConfiguration") GrpcConfiguration couaConfiguration) {
        this.couaConfiguration = couaConfiguration;
    }

    /**
     * Represents the coua grpc channel.
     *
     * @return Coua grpc channel.
     */
    @Bean(name = "couaChannel")
    public ManagedChannel getCouaChannel() {
        return ManagedChannelBuilder
                .forAddress(this.couaConfiguration.getHost(), this.couaConfiguration.getPort())
                .usePlaintext()
                .build();
    }

}
