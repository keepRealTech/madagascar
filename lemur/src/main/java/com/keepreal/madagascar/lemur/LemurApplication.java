package com.keepreal.madagascar.lemur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Represents the great entry of lemur service.
 */
@EnableCaching
@SpringBootApplication
public class LemurApplication {

    public static void main(String[] args) {
        SpringApplication.run(LemurApplication.class, args);
    }

}
