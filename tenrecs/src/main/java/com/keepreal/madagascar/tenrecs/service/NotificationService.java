package com.keepreal.madagascar.tenrecs.service;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.PageRequest;
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

    /**
     * Constructs the notification service.
     *
     * @param notificationRepository {@link NotificationRepository}.
     */
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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

}