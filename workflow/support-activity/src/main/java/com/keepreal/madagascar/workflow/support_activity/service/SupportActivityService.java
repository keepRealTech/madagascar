package com.keepreal.madagascar.workflow.support_activity.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.workflow.support_activity.model.SupportActivity;
import com.keepreal.madagascar.workflow.support_activity.repository.SupportActivityRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportActivityService {

    private final SupportActivityRepository supportActivityRepository;
    private final LongIdGenerator idGenerator;
    private final MongoTemplate mongoTemplate;

    private final static int MAX_CENTS = 5000;

    public SupportActivityService(SupportActivityRepository supportActivityRepository,
                                  LongIdGenerator idGenerator, MongoTemplate mongoTemplate) {
        this.supportActivityRepository = supportActivityRepository;
        this.idGenerator = idGenerator;
        this.mongoTemplate = mongoTemplate;
    }

    public void process() {
        List<String> payeeIdList = this.supportActivityRepository.findAllPayeeId();
        payeeIdList.forEach(payeeId -> {
            List<Long> maxAmounts = this.supportActivityRepository.findByUserId(payeeId);
            long sum = maxAmounts.stream().mapToLong(amount -> amount > MAX_CENTS ? MAX_CENTS : amount).sum();
            this.save(payeeId, sum, this.isCreateFeed(payeeId));
        });
    }

    private boolean isCreateFeed(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("fromHost").is(true));
        long count = mongoTemplate.count(query, "feedInfo");
        return count > 2;
    }

    private void save(String userId, Long amount, Boolean createFeed) {
        SupportActivity supportActivity = this.supportActivityRepository.findSupportActivityByUserIdAndDeletedIsFalse(userId);
        if (supportActivity == null) {
            supportActivity = new SupportActivity();
            supportActivity.setId(String.valueOf(idGenerator.nextId()));
            supportActivity.setUserId(userId);
        }

        if (createFeed && !supportActivity.getCreateFeed()) {
            supportActivity.setCreateFeed(true);
        }

        supportActivity.setAmount(amount);
        this.supportActivityRepository.save(supportActivity);
    }
}
