package com.keepreal.madagascar.workflow.updatePayment;

import com.keepreal.madagascar.workflow.updatePayment.service.UpdatePaymentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UpdatePaymentApplication implements CommandLineRunner {

    private final UpdatePaymentService updatePaymentService;

    public UpdatePaymentApplication(UpdatePaymentService updatePaymentService) {
        this.updatePaymentService = updatePaymentService;
    }

    public static void main(String[] args) {
        SpringApplication.run(UpdatePaymentApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        this.updatePaymentService.process();
    }
}
