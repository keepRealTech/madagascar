package com.keepreal.madagascar.baobob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Represents the big baobob <3.
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class BaobobApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaobobApplication.class, args);
    }

}
