package com.keepreal.madagascar.marty.config.grpc;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the grpc configurations for grpc.
 */
@Configuration
@Data
public class GrpcConfiguration {

    private String host;
    private Integer port;

}
