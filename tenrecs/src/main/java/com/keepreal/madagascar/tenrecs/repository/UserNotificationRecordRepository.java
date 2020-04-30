package com.keepreal.madagascar.tenrecs.repository;

import com.keepreal.madagascar.tenrecs.model.UserNotificationRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Represents the repository for user notification record.
 */
public interface UserNotificationRecordRepository extends MongoRepository<UserNotificationRecord, Long> {

    UserNotificationRecord findTopByUserIdAndIsDeletedIsFalse(String userId);

}
