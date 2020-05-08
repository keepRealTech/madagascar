package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.config.SystemNotificationConfiguration;
import com.keepreal.madagascar.lemur.dtoFactory.NotificationDTOFactory;
import com.keepreal.madagascar.lemur.service.NotificationService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.UnreadNotificationsCountMessage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.NotificationApi;
import swagger.model.NotificationsCountResponse;
import swagger.model.NotificationsResponse;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the notification controller.
 */
@RestController
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;
    private final NotificationDTOFactory notificationDTOFactory;

    /**
     * Constructs the notification controller.
     *
     * @param notificationService             {@link NotificationService}.
     * @param notificationDTOFactory          {@link NotificationDTOFactory}.
     */
    public NotificationController(NotificationService notificationService,
                                  NotificationDTOFactory notificationDTOFactory) {
        this.notificationService = notificationService;
        this.notificationDTOFactory = notificationDTOFactory;
    }

    /**
     * Implements the notification count api.
     *
     * @return {@link NotificationsCountResponse}.
     */
    @Override
    public ResponseEntity<NotificationsCountResponse> apiV1NotificationsCountGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        UnreadNotificationsCountMessage unreadNotificationsCountMessage = this.notificationService.countUnreadNotifications(userId);

        NotificationsCountResponse response = new NotificationsCountResponse();
        response.setData(this.notificationDTOFactory.valueOf(unreadNotificationsCountMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the notification get api.
     *
     * @param type     {@link NotificationType}.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link NotificationsResponse}.
     */
    @Cacheable(value = "default-system-notice-response", condition = "T(swagger.model.NotificationType).SYSTEM_NOTICE.equals(#type)")
    @Override
    public ResponseEntity<NotificationsResponse> apiV1NotificationsGet(swagger.model.NotificationType type,
                                                                       Integer page,
                                                                       Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();

        com.keepreal.madagascar.tenrecs.NotificationsResponse notificationsResponse;
        if (swagger.model.NotificationType.SYSTEM_NOTICE.equals(type)) {
            notificationsResponse = com.keepreal.madagascar.tenrecs.NotificationsResponse.newBuilder()
                    .addNotifications(this.defaultSystemNotificationMessage())
                    .build();
        } else {
            notificationsResponse =
                    this.notificationService.retrieveNotifications(userId, this.convertType(type), page, pageSize);
        }

        NotificationsResponse response = new NotificationsResponse();
        response.setData(notificationsResponse.getNotificationsList()
                .stream()
                .map(this.notificationDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(notificationsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Converts {@link swagger.model.NotificationType} to {@link NotificationType}.
     *
     * @param type {@link swagger.model.NotificationType}.
     * @return {@link NotificationType}.
     */
    private NotificationType convertType(swagger.model.NotificationType type) {
        if (Objects.isNull(type)) {
            return null;
        }

        switch (type) {
            case SYSTEM_NOTICE:
                return NotificationType.NOTIFICATION_SYSTEM_NOTICE;
            case COMMENTS:
                return NotificationType.NOTIFICATION_COMMENTS;
            case REACTIONS:
                return NotificationType.NOTIFICATION_REACTIONS;
            case ISLAND_NOTICE:
                return NotificationType.NOTIFICATION_ISLAND_NOTICE;
            default:
                return NotificationType.UNRECOGNIZED;
        }
    }

    /**
     * Constructs a default notification message entity.
     *
     * @return {@link NotificationMessage}.
     */
    private NotificationMessage defaultSystemNotificationMessage() {
        return NotificationMessage.newBuilder()
                .setType(NotificationType.NOTIFICATION_SYSTEM_NOTICE)
                .build();
    }

}
