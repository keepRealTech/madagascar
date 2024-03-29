package com.keepreal.madagascar.fossa.config.grpc;

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
    private final GrpcConfiguration vangaConfiguration;
    private final Tracer tracer;

    /**
     * Constructs the grpc channels factory.
     *
     * @param couaConfiguration Coua grpc configuration.
     * @param vangaConfiguration Vanga grpc configuration.
     * @param tracer            {@link Tracer}.
     */
    public GrpcChannelFactory(@Qualifier("couaConfiguration") GrpcConfiguration couaConfiguration,
                              @Qualifier("vangaConfiguration") GrpcConfiguration vangaConfiguration,
                              Tracer tracer) {
        this.couaConfiguration = couaConfiguration;
        this.vangaConfiguration = vangaConfiguration;
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
     * Represents the vanga grpc channel.
     *
     * @return Vanga grpc channel.
     */
    @Bean(name = "vangaChannel")
    public Channel getVangaChannel() {
        return TracingClientInterceptor
                .newBuilder()
                .withTracer(this.tracer)
                .build()
                .intercept(ManagedChannelBuilder
                        .forAddress(this.vangaConfiguration.getHost(), this.vangaConfiguration.getPort())
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
