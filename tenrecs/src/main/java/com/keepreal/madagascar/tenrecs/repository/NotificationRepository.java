package com.keepreal.madagascar.tenrecs.repository;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Represents the repository for notification.
 */
public interface NotificationRepository extends MongoRepository<Notification, String> {

    Long countByUserIdAndTypeIsAndCreatedAtAfterAndIsDeletedIsFalse(String userId, NotificationType type, Long timestamp);

    Page<Notification> findAllByUserIdAndTypeAndIsDeletedIsFalse(String userId, NotificationType type, Pageable pageable);

    Page<Notification> findAllByUserIdAndIsDeletedIsFalse(String userId, Pageable pageable);

    Optional<Notification> findByEventIdAndIsDeletedIsFalse(String eventId);

    Optional<Notification> findTopByNotice_SubscribeNotice_IslandIdAndNotice_SubscribeNotice_SubscriberIdAndIsDeletedIsFalseOrderByTimestamp(String islandId, String subscriberId);

    Optional<Notification> findTopByReaction_AuthorIdAndReaction_FeedIdAndIsDeletedIsFalseOrderByTimestamp(String authorId, String feedId);
}
