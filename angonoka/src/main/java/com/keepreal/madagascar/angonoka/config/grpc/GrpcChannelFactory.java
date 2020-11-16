package com.keepreal.madagascar.angonoka.config.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc channel factory.
 */
@Configuration
public class GrpcChannelFactory {

    private final GrpcConfiguration hawksbillConfiguration;

    /**
     * Constructs the grpc channels factory.
     *
     * @param hawksbillConfiguration hawksbill grpc configuration.
     */
    public GrpcChannelFactory(@Qualifier("hawksbillConfiguration") GrpcConfiguration hawksbillConfiguration) {
        this.hawksbillConfiguration = hawksbillConfiguration;
    }

    /**
     * Represents the hawksbill grpc channel.
     *
     * @return hawksbill grpc channel.
     */
    @Bean(name = "hawksbillChannel")
    public Channel getHawksbillChannel() {
        return ManagedChannelBuilder
                .forAddress(this.hawksbillConfiguration.getHost(), this.hawksbillConfiguration.getPort())
                .usePlaintext()
                .build();
    }

}
