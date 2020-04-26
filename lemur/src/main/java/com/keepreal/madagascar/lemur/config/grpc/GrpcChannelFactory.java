package com.keepreal.madagascar.lemur.config.grpc;

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

    private final GrpcConfiguration couaConfiguration;
    private final GrpcConfiguration fossaConfiguration;
    private final GrpcConfiguration baobobConfiguration;
    private final GrpcConfiguration indriConfiguration;
    private final GrpcConfiguration tenrecsConfiguration;

    /**
     * Constructs the grpc channels factory.
     *
     * @param couaConfiguration    Coua grpc configuration.
     * @param fossaConfiguration   Fossa grpc configuration.
     * @param baobobConfiguration  Baobob grpc configuration.
     * @param indriConfiguration   Indri grpc configuration.
     * @param tenrecsConfiguration Tencres grpc configuration.
     */
    public GrpcChannelFactory(@Qualifier("couaConfiguration") GrpcConfiguration couaConfiguration,
                              @Qualifier("fossaConfiguration") GrpcConfiguration fossaConfiguration,
                              @Qualifier("baobobConfiguration") GrpcConfiguration baobobConfiguration,
                              @Qualifier("indriConfiguration") GrpcConfiguration indriConfiguration,
                              @Qualifier("tenrecsConfiguration") GrpcConfiguration tenrecsConfiguration) {
        this.couaConfiguration = couaConfiguration;
        this.fossaConfiguration = fossaConfiguration;
        this.baobobConfiguration = baobobConfiguration;
        this.indriConfiguration = indriConfiguration;
        this.tenrecsConfiguration = tenrecsConfiguration;
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

    /**
     * Represents the fossa grpc channel.
     *
     * @return Fossa grpc channel.
     */
    @Bean(name = "fossaChannel")
    public ManagedChannel getFossaChannel() {
        return ManagedChannelBuilder
                .forAddress(this.couaConfiguration.getHost(), this.couaConfiguration.getPort())
                .usePlaintext()
                .build();
    }

    /**
     * Represents the indri grpc channel.
     *
     * @return Indri grpc channel.
     */
    @Bean(name = "indriChannel")
    public ManagedChannel getIndriChannel() {
        return ManagedChannelBuilder
                .forAddress(this.couaConfiguration.getHost(), this.couaConfiguration.getPort())
                .usePlaintext()
                .build();
    }

    /**
     * Represents the baobob grpc channel.
     *
     * @return Baobob grpc channel.
     */
    @Bean(name = "baobobChannel")
    public ManagedChannel getBaobobChannel() {
        return ManagedChannelBuilder
                .forAddress(this.couaConfiguration.getHost(), this.couaConfiguration.getPort())
                .usePlaintext()
                .build();
    }

    /**
     * Represents the tenrecs grpc channel.
     *
     * @return Tenrecs grpc channel.
     */
    @Bean(name = "tenrecsChannel")
    public ManagedChannel getTenrecsChannel() {
        return ManagedChannelBuilder
                .forAddress(this.couaConfiguration.getHost(), this.couaConfiguration.getPort())
                .usePlaintext()
                .build();
    }

}
