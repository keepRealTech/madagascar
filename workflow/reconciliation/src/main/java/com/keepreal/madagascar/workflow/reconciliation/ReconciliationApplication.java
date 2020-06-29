package com.keepreal.madagascar.workflow.reconciliation;

import com.keepreal.madagascar.workflow.reconciliation.service.ReconciliationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReconciliationApplication implements CommandLineRunner {

    private final ReconciliationService reconciliationService;

    public ReconciliationApplication(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ReconciliationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        reconciliationService.run();
    }
}
