package com.keepreal.madagascar.indri.config;

import io.grpc.ServerInterceptor;
import io.opentracing.Tracer;
import io.opentracing.contrib.grpc.TracingServerInterceptor;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc server configuration.
 */
@Configuration
public class GrpcServerConfiguration {

    private final Tracer tracer;

    /**
     * Constructs the GRpc server configurations.
     *
     * @param tracer {@link Tracer}.
     */
    public GrpcServerConfiguration(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Represents the grpc tracing server interceptor.
     *s
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
