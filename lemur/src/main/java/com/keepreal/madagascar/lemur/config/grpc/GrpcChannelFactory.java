package com.keepreal.madagascar.lemur.config.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
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

    private final GrpcConfiguration couaConfiguration;
    private final GrpcConfiguration fossaConfiguration;
    private final GrpcConfiguration baobobConfiguration;
    private final GrpcConfiguration indriConfiguration;
    private final GrpcConfiguration tenrecsConfiguration;
    private final TracingClientInterceptor interceptor;

    /**
     * Constructs the grpc channels factory.
     *
     * @param couaConfiguration    Coua grpc configuration.
     * @param fossaConfiguration   Fossa grpc configuration.
     * @param baobobConfiguration  Baobob grpc configuration.
     * @param indriConfiguration   Indri grpc configuration.
     * @param tenrecsConfiguration Tencres grpc configuration.
     * @param tracer               {@link Tracer}.
     */
    public GrpcChannelFactory(@Qualifier("couaConfiguration") GrpcConfiguration couaConfiguration,
                              @Qualifier("fossaConfiguration") GrpcConfiguration fossaConfiguration,
                              @Qualifier("baobobConfiguration") GrpcConfiguration baobobConfiguration,
                              @Qualifier("indriConfiguration") GrpcConfiguration indriConfiguration,
                              @Qualifier("tenrecsConfiguration") GrpcConfiguration tenrecsConfiguration,
                              Tracer tracer) {
        this.couaConfiguration = couaConfiguration;
        this.fossaConfiguration = fossaConfiguration;
        this.baobobConfiguration = baobobConfiguration;
        this.indriConfiguration = indriConfiguration;
        this.tenrecsConfiguration = tenrecsConfiguration;
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
        return this.interceptor.intercept(NettyChannelBuilder
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

}
