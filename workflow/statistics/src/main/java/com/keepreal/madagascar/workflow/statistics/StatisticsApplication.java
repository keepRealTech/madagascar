package com.keepreal.madagascar.workflow.statistics;

import com.keepreal.madagascar.common.workflow.annotation.EnableWorkflowService;
import com.keepreal.madagascar.workflow.statistics.service.PaymentService;
import com.keepreal.madagascar.workflow.statistics.service.StatisticsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Represents the settler application workflow entrance.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableWorkflowService
public class StatisticsApplication implements CommandLineRunner {

    private final StatisticsService statisticsService;
    private final PaymentService paymentService;

    /**
     * Constructs the statistics application.
     *
     * @param statisticsService {@link StatisticsService}.
     * @param paymentService    {@link PaymentService}.
     */
    public StatisticsApplication(StatisticsService statisticsService,
                                 PaymentService paymentService) {
        this.statisticsService = statisticsService;
        this.paymentService = paymentService;
    }

    /**
     * Main function.
     *
     * @param args Args.
     */
    public static void main(String[] args) {
        SpringApplication.run(StatisticsApplication.class, args);
    }

    /**
     * The entry point for the workflow.
     *
     * @param args Arguments.
     */
    @Override
    public void run(String... args) {
        this.statisticsService.run();
        this.paymentService.run();
        System.exit(0);
    }

}