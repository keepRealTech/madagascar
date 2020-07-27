package com.keepreal.madagascar.marty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MartyApplication.class, args);
    }
}
