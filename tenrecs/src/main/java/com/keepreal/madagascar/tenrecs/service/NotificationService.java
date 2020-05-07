package com.keepreal.madagascar.tenrecs.service;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.snowflake.generator.DefaultSnowflakeIdGenerator;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.repository.NotificationRepository;
import com.keepreal.madagascar.tenrecs.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Represents the notification service.
 */
@Service
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
                this.notificationRepository.countByUserIdAndTypeIsAndCreatedAtAfter(userId, type, timestamp));
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
    public Page<Notification> retrieveByUSerIdWithPagination(String userId, PageRequest pageRequest) {
        return this.notificationRepository.findAllByUserIdAndIsDeletedIsFalse(userId, PaginationUtils.valueOf(pageRequest));
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
     * Checks if an event id has been consumed.
     *
     * @param eventId Event id.
     * @return True if has consumed.
     */
    public boolean hasConsumed(String eventId) {
        return this.notificationRepository.findByEventIdAndIsDeletedIsFalse(eventId).isPresent();
    }

}