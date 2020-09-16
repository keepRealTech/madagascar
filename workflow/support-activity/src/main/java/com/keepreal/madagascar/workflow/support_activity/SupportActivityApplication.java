package com.keepreal.madagascar.workflow.support_activity;

import com.keepreal.madagascar.common.snowflake.annotation.EnableIdGenerator;
import com.keepreal.madagascar.workflow.support_activity.service.SupportActivityService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableIdGenerator
@EnableJpaAuditing
public class SupportActivityApplication implements CommandLineRunner {

    private final SupportActivityService supportActivityService;

    public SupportActivityApplication(SupportActivityService supportActivityService) {
        this.supportActivityService = supportActivityService;
    }

    public static void main(String[] args) {
        SpringApplication.run(SupportActivityApplication.class, args);
    }

    @Override
    public void run(String... args) {
        this.supportActivityService.process();
    }
}
