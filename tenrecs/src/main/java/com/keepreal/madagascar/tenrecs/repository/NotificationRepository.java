package com.keepreal.madagascar.tenrecs.repository;

import com.keepreal.madagascar.common.NoticeType;
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

    Long countByUserIdAndTypeAndTimestampAfterAndIsDeletedIsFalse(String userId, NotificationType type, Long timestamp);

    Long countByUserIdAndNotice_TypeAndTimestampAfterAndIsDeletedIsFalse(String userId, NoticeType type, Long timestamp);

    Long countByUserIdAndNotice_TypeInAndTimestampAfterAndIsDeletedIsFalse(String userId, Iterable<NoticeType> type, Long timestamp);

    Page<Notification> findAllByUserIdAndTypeAndIsDeletedIsFalse(String userId, NotificationType type, Pageable pageable);

    Page<Notification> findAllByUserIdAndNotice_TypeAndIsDeletedIsFalse(String userId, NoticeType type, Pageable pageable);

    Page<Notification> findAllByUserIdAndNotice_TypeInAndIsDeletedIsFalse(String userId, Iterable<NoticeType> types, Pageable pageable);

    Page<Notification> findAllByUserIdAndIsDeletedIsFalse(String userId, Pageable pageable);

    boolean existsByEventIdAndIsDeletedIsFalse(String eventId);

    Optional<Notification> findTopByNotice_SubscribeNotice_IslandIdAndNotice_SubscribeNotice_SubscriberIdAndIsDeletedIsFalseOrderByTimestamp(String islandId, String subscriberId);

    Optional<Notification> findTopByReaction_AuthorIdAndReaction_FeedIdAndIsDeletedIsFalseOrderByTimestamp(String authorId, String feedId);
}
