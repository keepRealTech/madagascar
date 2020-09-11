package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.ActivityMessage;
import com.keepreal.madagascar.vanga.model.SupportActivity;
import com.keepreal.madagascar.vanga.repository.SupportActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class SupportActivityService {

    private final SupportActivityRepository supportActivityRepository;

    private final static int TOTAL_CENTS = 888800;
    private final static int CREATE_FEED_CENTS = 880;
    private final static int DEFAULT_RATIO = 10;

    public SupportActivityService(SupportActivityRepository supportActivityRepository) {
        this.supportActivityRepository = supportActivityRepository;
    }

    public ActivityMessage getActivityInfo(String userId) {
        SupportActivity supportActivity = this.supportActivityRepository.findSupportActivityByUserIdAndDeletedIsFalse(userId);

        if (supportActivity == null) {
            return ActivityMessage.newBuilder()
                    .setRatio(DEFAULT_RATIO)
                    .setGained(0L)
                    .setToGain(TOTAL_CENTS)
                    .build();
        }

        Integer activityPercent = supportActivity.getActivityPercent();
        Long amount = supportActivity.getAmount();
        Boolean createFeed = supportActivity.getCreateFeed();

        if (createFeed) {
            amount += CREATE_FEED_CENTS;
        }

        long gained = amount * activityPercent / 100;
        long toGain = TOTAL_CENTS - gained;

        return ActivityMessage.newBuilder()
                .setRatio(activityPercent)
                .setToGain(toGain)
                .setGained(gained)
                .build();
    }
}
