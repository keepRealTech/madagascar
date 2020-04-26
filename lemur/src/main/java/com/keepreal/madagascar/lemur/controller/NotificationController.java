package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.lemur.service.NotificationService;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import swagger.api.NotificationApi;
import swagger.model.NotificationType;
import swagger.model.NotificationsCountResponse;
import swagger.model.NotificationsResponse;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Represents the notification controller.
 */
@RestController
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public ResponseEntity<NotificationsCountResponse> apiV1NotificationsCountGet() {


        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<NotificationsResponse> apiV1NotificationsGet(NotificationType type, Integer page, Integer pageSize) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
