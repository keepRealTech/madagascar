package com.keepreal.madagascar.marty.config.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerInterceptor;
import io.opentracing.Tracer;
import io.opentracing.contrib.grpc.TracingClientInterceptor;
import io.opentracing.contrib.grpc.TracingServerInterceptor;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc channel factory.
 */
@Configuration
public class GrpcChannelFactory {

    private final GrpcConfiguration couaConfiguration;
    private final GrpcConfiguration fossaConfiguration;
    private final GrpcConfiguration asityConfiguration;
    private final Tracer tracer;

    /**
     * Constructs the grpc channels factory.
     *
     * @param couaConfiguration  Coua grpc configuration.
     * @param fossaConfiguration Fossa grpc configuration.
     * @param asityConfiguration Asity grpc configuration.
     * @param tracer             {@link Tracer}.
     */
    public GrpcChannelFactory(@Qualifier("couaConfiguration") GrpcConfiguration couaConfiguration,
                              @Qualifier("fossaConfiguration") GrpcConfiguration fossaConfiguration,
                              @Qualifier("asityConfiguration") GrpcConfiguration asityConfiguration,
                              Tracer tracer) {
        this.couaConfiguration = couaConfiguration;
        this.fossaConfiguration = fossaConfiguration;
        this.asityConfiguration = asityConfiguration;
        this.tracer = tracer;
    }

    /**
     * Represents the coua grpc channel.
     *
     * @return Coua grpc channel.
     */
    @Bean(name = "couaChannel")
    public Channel getCouaChannel() {
        return TracingClientInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .build()
                .intercept(ManagedChannelBuilder
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
        return TracingClientInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .build()
                .intercept(ManagedChannelBuilder
                        .forAddress(this.fossaConfiguration.getHost(), this.fossaConfiguration.getPort())
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
        return TracingClientInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .build()
                .intercept(ManagedChannelBuilder
                        .forAddress(this.asityConfiguration.getHost(), this.asityConfiguration.getPort())
                        .usePlaintext()
                        .build());
    }

    /**
     * Represents the grpc tracing server interceptor.
     * s
     *
     * @return {@link TracingServerInterceptor}.
     */
    @Bean
    @ConditionalOnProperty(value = "opentracing.jaeger.grpc-server-interceptor", havingValue = "true")
    @GRpcGlobalInterceptor
    public ServerInterceptor globalServerInterceptor() {
        return TracingServerInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .withStreaming()
                .withVerbosity()
                .withTracedAttributes(TracingServerInterceptor.ServerRequestAttribute.HEADERS,
                        TracingServerInterceptor.ServerRequestAttribute.METHOD_TYPE)
                .build();
    }

}
