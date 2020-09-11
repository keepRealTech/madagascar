package com.keepreal.madagascar.workflow.supportActivity.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.workflow.supportActivity.model.SupportActivity;
import com.keepreal.madagascar.workflow.supportActivity.repository.SupportActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportActivityService {

    private final SupportActivityRepository supportActivityRepository;
    private final LongIdGenerator idGenerator;

    private final static int MAX_CENTS = 5000;

    public SupportActivityService(SupportActivityRepository supportActivityRepository,
                                  LongIdGenerator idGenerator) {
        this.supportActivityRepository = supportActivityRepository;
        this.idGenerator = idGenerator;
    }

    public void process() {
        List<String> payeeIdList = this.supportActivityRepository.findAllPayeeId();
        payeeIdList.forEach(payeeId -> {
            List<Long> maxAmounts = this.supportActivityRepository.findByUserId(payeeId);
            long sum = maxAmounts.stream().mapToLong(amount -> amount > MAX_CENTS ? MAX_CENTS : amount).sum();
            this.save(payeeId, sum);
        });
    }

    private void save(String userId, Long amount) {
        SupportActivity supportActivity = this.supportActivityRepository.findSupportActivityByUserIdAndDeletedIsFalse(userId);
        if (supportActivity == null) {
            supportActivity = new SupportActivity();
            supportActivity.setId(String.valueOf(idGenerator.nextId()));
            supportActivity.setUserId(userId);
        }

        supportActivity.setAmount(amount);
        this.supportActivityRepository.save(supportActivity);
    }
}
