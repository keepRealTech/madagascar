package com.keepreal.madagascar.tenrecs.service;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.snowflake.generator.DefaultSnowflakeIdGenerator;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.repository.NotificationRepository;
import com.keepreal.madagascar.tenrecs.util.PaginationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Represents the notification service.
 */
@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final LongIdGenerator idGenerator;

    /**
     * Constructs the notification service.
     *
     * @param notificationRepository {@link NotificationRepository}.
     * @param idGenerator            {@link DefaultSnowflakeIdGenerator}.
     */
    public NotificationService(NotificationRepository notificationRepository,
                               LongIdGenerator idGenerator) {
        this.notificationRepository = notificationRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Counts the notifications.
     *
     * @param userId    User id.
     * @param type      {@link NotificationType}.
     * @param timestamp Created timestamp after.
     * @return Count.
     */
    public int countByUserIdAndTypeAndCreatedAtAfter(String userId, NotificationType type, long timestamp) {
        return Math.toIntExact(
                this.notificationRepository.countByUserIdAndTypeIsAndTimestampAfterAndIsDeletedIsFalse(userId, type, timestamp));
    }

    /**
     * Retrieves notifications by user id and type.
     *
     * @param userId      User id.
     * @param type        {@link NotificationType}.
     * @param pageRequest {@link PageRequest}.
     * @return {@link Notification}.
     */
    public Page<Notification> retrieveByUserIdAndTypeWithPagination(String userId, NotificationType type, PageRequest pageRequest) {
        return this.notificationRepository.findAllByUserIdAndTypeAndIsDeletedIsFalse(userId, type, PaginationUtils.valueOf(pageRequest));
    }

    /**
     * Retrieves notifications by user id.
     *
     * @param userId      User id.
     * @param pageRequest {@link PageRequest}.
     * @return {@link Notification}.
     */
    public Page<Notification> retrieveByUserIdWithPagination(String userId, PageRequest pageRequest) {
        return this.notificationRepository.findAllByUserIdAndIsDeletedIsFalse(userId, PaginationUtils.valueOf(pageRequest));
    }

    /**
     * Retrieves the latest notification by user id and feed id.
     *
     * @param authorId User id.
     * @param feedId Feed id.
     * @return {@link Notification}.
     */
    public Optional<Notification> retrieveLastByReactionAuthorIdAndReactionFeedId(String authorId, String feedId) {
        return this.notificationRepository.findTopByReaction_AuthorIdAndReaction_FeedIdAndIsDeletedIsFalseOrderByTimestamp(authorId, feedId);
    }

    /**
     * Retrieves the latest subscribe notification by island id and subscriber id.
     *
     * @param islandId Island id.
     * @param subscriberId Subscriber id.
     * @return {@link Notification}.
     */
    public Optional<Notification> retrieveLastSubscribeNoticeByIslandIdAndSubscriberId(String islandId, String subscriberId) {
        return this.notificationRepository.findTopByNotice_SubscribeNotice_IslandIdAndNotice_SubscribeNotice_SubscriberIdAndIsDeletedIsFalseOrderByTimestamp(islandId, subscriberId);
    }

    /**
     * Inserts the notification.
     *
     * @param notification {@link Notification}.
     * @return {@link Notification}.
     */
    public Notification insert(Notification notification) {
        notification.setId(String.valueOf(this.idGenerator.nextId()));
        notification.setCreatedAt(notification.getTimestamp());
        return this.notificationRepository.insert(notification);
    }

    /**
     * Updates the notification.
     *
     * @param notification {@link Notification}.
     * @return {@link Notification}.
     */
    public Notification update(Notification notification) {
        return this.notificationRepository.save(notification);
    }

    /**
     * Checks if an event id has been consumed.
     *
     * @param eventId Event id.
     * @return True if has consumed.
     */
    public boolean hasConsumed(String eventId) {
        return this.notificationRepository.existsByEventIdAndIsDeletedIsFalse(eventId);
    }

}