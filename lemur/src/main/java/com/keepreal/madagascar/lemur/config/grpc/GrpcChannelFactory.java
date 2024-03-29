package com.keepreal.madagascar.lemur.config.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.opentracing.Tracer;
import io.opentracing.contrib.grpc.TracingClientInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc channel factory.
 */
@Configuration
public class GrpcChannelFactory {

    private final GrpcConfiguration asityConfiguration;
    private final GrpcConfiguration couaConfiguration;
    private final GrpcConfiguration fossaConfiguration;
    private final GrpcConfiguration baobobConfiguration;
    private final GrpcConfiguration indriConfiguration;
    private final GrpcConfiguration tenrecsConfiguration;
    private final GrpcConfiguration mantellaConfiguration;
    private final GrpcConfiguration vangaConfiguration;
    private final GrpcConfiguration hoopoeConfiguration;
    private final GrpcConfiguration angonokaConfiguration;
    private final GrpcConfiguration hawksbillConfiguration;
    private final TracingClientInterceptor interceptor;

    /**
     * Constructs the grpc channels factory.
     *
     * @param asityConfiguration    Asity grpc configuration.
     * @param couaConfiguration     Coua grpc configuration.
     * @param fossaConfiguration    Fossa grpc configuration.
     * @param baobobConfiguration   Baobob grpc configuration.
     * @param indriConfiguration    Indri grpc configuration.
     * @param tenrecsConfiguration  Tencres grpc configuration.
     * @param mantellaConfiguration Mantella grpc configuration.
     * @param vangaConfiguration    Vanga grpc configuration.
     * @param angonokaConfiguration Angonoka grpc configuration.
     * @param hawksbillConfiguration Hawksbill grpc configuration.
     * @param tracer                {@link Tracer}.
     */
    public GrpcChannelFactory(@Qualifier("asityConfiguration") GrpcConfiguration asityConfiguration,
                              @Qualifier("couaConfiguration") GrpcConfiguration couaConfiguration,
                              @Qualifier("fossaConfiguration") GrpcConfiguration fossaConfiguration,
                              @Qualifier("baobobConfiguration") GrpcConfiguration baobobConfiguration,
                              @Qualifier("indriConfiguration") GrpcConfiguration indriConfiguration,
                              @Qualifier("tenrecsConfiguration") GrpcConfiguration tenrecsConfiguration,
                              @Qualifier("mantellaConfiguration") GrpcConfiguration mantellaConfiguration,
                              @Qualifier("vangaConfiguration") GrpcConfiguration vangaConfiguration,
                              @Qualifier("hoopoeConfiguration") GrpcConfiguration hoopoeConfiguration,
                              @Qualifier("angonokaConfiguration") GrpcConfiguration angonokaConfiguration,
                              @Qualifier("hawksbillConfiguration") GrpcConfiguration hawksbillConfiguration,
                              Tracer tracer) {
        this.asityConfiguration = asityConfiguration;
        this.couaConfiguration = couaConfiguration;
        this.fossaConfiguration = fossaConfiguration;
        this.baobobConfiguration = baobobConfiguration;
        this.indriConfiguration = indriConfiguration;
        this.tenrecsConfiguration = tenrecsConfiguration;
        this.mantellaConfiguration = mantellaConfiguration;
        this.vangaConfiguration = vangaConfiguration;
        this.hoopoeConfiguration = hoopoeConfiguration;
        this.angonokaConfiguration = angonokaConfiguration;
        this.hawksbillConfiguration = hawksbillConfiguration;
        this.interceptor = TracingClientInterceptor
                .newBuilder()
                .withTracer(tracer)
                .build();
    }

    /**
     * Represents the coua grpc channel.
     *
     * @return Coua grpc channel.
     */
    @Bean(name = "couaChannel")
    public Channel getCouaChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.couaConfiguration.getHost(), this.couaConfiguration.getPort())
                .usePlaintext()
                .build());
    }

    /**
     * Represents the fossa grpc channel.
     *
     * @return Fossa grpc channel.
     */
    @Bean(name = "fossaChannel")
    public Channel getFossaChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.fossaConfiguration.getHost(), this.fossaConfiguration.getPort())
                .usePlaintext()
                .build());
    }

    /**
     * Represents the indri grpc channel.
     *
     * @return Indri grpc channel.
     */
    @Bean(name = "indriChannel")
    public Channel getIndriChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.indriConfiguration.getHost(), this.indriConfiguration.getPort())
                .usePlaintext()
                .maxInboundMessageSize(6291456)
                .maxInboundMetadataSize(6291456)
                .build());
    }

    /**
     * Represents the baobob grpc channel.
     *
     * @return Baobob grpc channel.
     */
    @Bean(name = "baobobChannel")
    public Channel getBaobobChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.baobobConfiguration.getHost(), this.baobobConfiguration.getPort())
                .usePlaintext()
                .build());
    }

    /**
     * Represents the tenrecs grpc channel.
     *
     * @return Tenrecs grpc channel.
     */
    @Bean(name = "tenrecsChannel")
    public Channel getTenrecsChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.tenrecsConfiguration.getHost(), this.tenrecsConfiguration.getPort())
                .usePlaintext()
                .build());
    }

    /**
     * Represents the mantella grpc channel.
     *
     * @return Mantella grpc channel.
     */
    @Bean(name = "mantellaChannel")
    public Channel getMantellaChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.mantellaConfiguration.getHost(), this.mantellaConfiguration.getPort())
                .usePlaintext()
                .build());
    }

    /**
     * Represents the vanga grpc channel.
     *
     * @return Vanga grpc channel.
     */
    @Bean(name = "vangaChannel")
    public Channel getVangaChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.vangaConfiguration.getHost(), this.vangaConfiguration.getPort())
                .usePlaintext()
                .build());
    }

    /**
     * Represents the asity grpc channel.
     *
     * @return Asity grpc channel.
     */
    @Bean(name = "asityChannel")
    public Channel getAsityChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.asityConfiguration.getHost(), this.asityConfiguration.getPort())
                .usePlaintext()
                .build());
    }

    /**
     * Represents the hoopoe grpc channel.
     *
     * @return Hoopoe grpc channel.
     */
    @Bean(name = "hoopoeChannel")
    public Channel getHoopoeChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.hoopoeConfiguration.getHost(), this.hoopoeConfiguration.getPort())
                .usePlaintext()
                .build());
    }

    /**
     * Represents the angonoka grpc channel.
     *
     * @return Angonoka grpc channel.
     */
    @Bean(name = "angonokaChannel")
    public Channel getAngonokaChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.angonokaConfiguration.getHost(), this.angonokaConfiguration.getPort())
                .usePlaintext()
                .build());
    }

    /**
     * Represents the hawksbill grpc channel.
     *
     * @return Hawksbill grpc channel.
     */
    @Bean(name = "hawksbillChannel")
    public Channel getHawksbillChannel() {
        return this.interceptor.intercept(ManagedChannelBuilder
                .forAddress(this.hawksbillConfiguration.getHost(), this.hawksbillConfiguration.getPort())
                .usePlaintext()
                .build());
    }

}
