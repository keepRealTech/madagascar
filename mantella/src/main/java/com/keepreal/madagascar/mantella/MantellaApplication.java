package com.keepreal.madagascar.mantella;

import com.keepreal.madagascar.common.snowflake.annotation.EnableIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Represents the Mantella spring boot service entry.
 */
@SpringBootApplication
@EnableMongoAuditing
@EnableIdGenerator
public class MantellaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MantellaApplication.class, args);
    }

}
