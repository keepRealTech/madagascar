package com.keepreal.madagascar.tenrecs;

import com.keepreal.madagascar.common.snowflake.annotation.EnableIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Represents the Tenrecs spring boot service entry.
 */
@SpringBootApplication
@EnableMongoAuditing
@EnableIdGenerator
public class TenrecsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenrecsApplication.class, args);
    }

}
