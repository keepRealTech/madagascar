package com.keepreal.madagascar.tenrecs.service;

import com.keepreal.madagascar.tenrecs.model.UserNotificationRecord;
import com.keepreal.madagascar.tenrecs.repository.UserNotificationRecordRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the user notification record service.
 */
@Service
public class UserNotificationRecordService {

    private final UserNotificationRecordRepository userNotificationRecordRepository;

    /**
     * Constructs the user notification record service.
     *
     * @param userNotificationRecordRepository {@link UserNotificationRecordRepository}.
     */
    public UserNotificationRecordService(UserNotificationRecordRepository userNotificationRecordRepository) {
        this.userNotificationRecordRepository = userNotificationRecordRepository;
    }

    /**
     * Upserts a given {@link UserNotificationRecord}.
     *
     * @param userNotificationRecord {@link UserNotificationRecord}.
     * @return {@link UserNotificationRecord}.
     */
    public UserNotificationRecord upsert(UserNotificationRecord userNotificationRecord) {
        return this.userNotificationRecordRepository.save(userNotificationRecord);
    }

    /**
     * Retrieves a {@link UserNotificationRecord} by user id, create one if not exists.
     *
     * @param userId User id.
     * @return {@link UserNotificationRecord}.
     */
    public UserNotificationRecord retrieveByUserId(String userId) {
        UserNotificationRecord userNotificationRecord
                = this.userNotificationRecordRepository.findTopByUserIdAndIsDeletedIsFalse(userId);

        if (Objects.nonNull(userNotificationRecord)) {
            return userNotificationRecord;
        }

        return this.upsert(UserNotificationRecord.builder().userId(userId).build());
    }

}
