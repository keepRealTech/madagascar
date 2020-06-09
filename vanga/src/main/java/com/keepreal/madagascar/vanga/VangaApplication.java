package com.keepreal.madagascar.vanga;

import com.keepreal.madagascar.common.snowflake.annotation.EnableIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Represents the great entry of vanga service.
 */
@SpringBootApplication
@EnableIdGenerator
public class VangaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VangaApplication.class, args);
    }

}
