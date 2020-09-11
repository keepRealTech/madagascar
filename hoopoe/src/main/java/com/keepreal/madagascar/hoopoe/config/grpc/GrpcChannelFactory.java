package com.keepreal.madagascar.hoopoe.config.grpc;

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
    private final Tracer tracer;

    /**
     * Constructs the grpc channels factory.
     *
     * @param couaConfiguration     coua grpc configuration.
     * @param tracer                {@link Tracer}.
     */
    public GrpcChannelFactory(@Qualifier("couaConfiguration") GrpcConfiguration couaConfiguration,
                              Tracer tracer) {
        this.couaConfiguration = couaConfiguration;
        this.tracer = tracer;
    }

    /**
     * Represents the coua grpc channel.
     *
     * @return Fossa grpc channel.
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
