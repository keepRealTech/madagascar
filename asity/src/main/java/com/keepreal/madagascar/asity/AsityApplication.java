package com.keepreal.madagascar.asity;

import com.keepreal.madagascar.common.snowflake.annotation.EnableIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Represents the asity application entrance.
 */
@SpringBootApplication
@EnableIdGenerator
@EnableJpaAuditing
public class AsityApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsityApplication.class, args);
    }

}
