package com.keepreal.madagascar.workflow.settler;

import com.keepreal.madagascar.workflow.settler.service.SettlerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Represents the settler application workflow entrance.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
public class SettlerApplication implements CommandLineRunner {

    private final SettlerService settlerService;

    /**
     * Constructs the settler application.
     *
     * @param settlerService {@link SettlerService}.
     */
    public SettlerApplication(SettlerService settlerService) {
        this.settlerService = settlerService;
    }

    /**
     * Main function.
     *
     * @param args Args.
     */
    public static void main(String[] args) {
        SpringApplication.run(SettlerApplication.class, args);
    }

    /**
     * The entry point for the workflow.
     *
     * @param args Arguments.
     */
    @Override
    public void run(String... args) {
        this.settlerService.run(args);
        System.exit(0);
    }

}