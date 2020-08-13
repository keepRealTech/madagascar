package com.keepreal.madagascar.lemur;

import com.keepreal.madagascar.common.stats_events.annotation.EnableStatsEventsProducer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Represents the great entry of lemur service.
 */
@EnableStatsEventsProducer
@SpringBootApplication
public class LemurApplication {

    public static void main(String[] args) {
        SpringApplication.run(LemurApplication.class, args);
    }

}
