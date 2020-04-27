package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.dtoFactory.NotificationDTOFactory;
import com.keepreal.madagascar.lemur.service.NotificationService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.lemur.util.ResponseUtils;
import com.keepreal.madagascar.tenrecs.UnreadNotificationsCountMessage;
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
     * @param notificationService    {@link NotificationService}.
     * @param notificationDTOFactory {@link NotificationDTOFactory}.
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
    @Override
    public ResponseEntity<NotificationsResponse> apiV1NotificationsGet(swagger.model.NotificationType type,
                                                                       Integer page,
                                                                       Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();

        com.keepreal.madagascar.tenrecs.NotificationsResponse notificationsResponse =
                this.notificationService.retrieveNotifications(userId, this.convertType(type), page, pageSize);

        NotificationsResponse response = new NotificationsResponse();
        response.setData(notificationsResponse.getNotificationList()
                .stream()
                .map(this.notificationDTOFactory::valueOf)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(notificationsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(HttpStatus.OK);
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

}
