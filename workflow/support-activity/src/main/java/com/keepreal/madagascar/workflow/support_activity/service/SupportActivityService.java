package com.keepreal.madagascar.workflow.support_activity.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.workflow.support_activity.model.vanga.SupportActivity;
import com.keepreal.madagascar.workflow.support_activity.repository.coua.IslandRepository;
import com.keepreal.madagascar.workflow.support_activity.repository.vanga.SupportActivityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SupportActivityService {

    private final SupportActivityRepository supportActivityRepository;
    private final IslandRepository islandRepository;
    private final LongIdGenerator idGenerator;
    private final MongoTemplate mongoTemplate;

    private final static int MAX_CENTS = 5000;
    private final static long STARTED_TIME = 1599494400000L;

    public SupportActivityService(SupportActivityRepository supportActivityRepository,
                                  IslandRepository islandRepository,
                                  LongIdGenerator idGenerator,
                                  MongoTemplate mongoTemplate) {
        this.supportActivityRepository = supportActivityRepository;
        this.islandRepository = islandRepository;
        this.idGenerator = idGenerator;
        this.mongoTemplate = mongoTemplate;
    }

    public void process() {
        List<String> userIds = this.islandRepository.findHostIds();
        userIds.forEach(userId -> {
            List<Long> maxAmounts = this.supportActivityRepository.findByUserId(userId);
            long sum = maxAmounts.stream().mapToLong(amount -> amount > MAX_CENTS ? MAX_CENTS : amount).sum();
            this.save(userId, sum, this.isCreateFeed(userId));
        });
    }

    private boolean isCreateFeed(String userId) {
        Long createdTime = this.islandRepository.findIslandCreatedTimeByHostId(userId);
        if (createdTime == null || createdTime < STARTED_TIME) {
            return false;
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("fromHost").is(true));
        long count = mongoTemplate.count(query, "feedInfo");
        return count > 1;
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
