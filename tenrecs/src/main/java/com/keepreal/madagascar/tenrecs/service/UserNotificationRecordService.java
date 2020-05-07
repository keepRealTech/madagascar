package com.keepreal.madagascar.tenrecs.service;

import com.keepreal.madagascar.common.snowflake.generator.DefaultSnowflakeIdGenerator;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
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
    private final LongIdGenerator idGenerator;

    /**
     * Constructs the user notification record service.
     *
     * @param userNotificationRecordRepository {@link UserNotificationRecordRepository}.
     * @param idGenerator                      {@link DefaultSnowflakeIdGenerator}.
     */
    public UserNotificationRecordService(UserNotificationRecordRepository userNotificationRecordRepository,
                                         LongIdGenerator idGenerator) {
        this.userNotificationRecordRepository = userNotificationRecordRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Inserts a given {@link UserNotificationRecord}.
     *
     * @param userNotificationRecord {@link UserNotificationRecord}.
     * @return {@link UserNotificationRecord}.
     */
    public UserNotificationRecord insert(UserNotificationRecord userNotificationRecord) {
        userNotificationRecord.setId(String.valueOf(this.idGenerator.nextId()));
        return this.userNotificationRecordRepository.insert(userNotificationRecord);
    }

    /**
     * Updates a given {@link UserNotificationRecord}.
     *
     * @param userNotificationRecord {@link UserNotificationRecord}.
     * @return {@link UserNotificationRecord}.
     */
    public UserNotificationRecord update(UserNotificationRecord userNotificationRecord) {
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

        return this.insert(UserNotificationRecord.builder().userId(userId).build());
    }

}
