package com.keepreal.madagascar.tenrecs.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.factory.NotificationFactory;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the logic consuming {@link NotificationEvent}.
 */
@Component
@Slf4j
public class NotificationEventListener implements MessageListener {

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;

    /**
     * Constructs the notification event listener.
     *
     * @param notificationService {@link NotificationService}.
     * @param notificationFactory {@link NotificationFactory}.
     */
    public NotificationEventListener(NotificationService notificationService,
                                     NotificationFactory notificationFactory) {
        this.notificationService = notificationService;
        this.notificationFactory = notificationFactory;
    }

    /**
     * Implements the logic after consumption.
     *
     * @param message Message payload.
     * @param context Context.
     * @return {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (this.notificationService.hasConsumed(message.getKey())) {
                log.warn("Event id {} has been consumed before, skipped.", message.getKey());
                return Action.CommitMessage;
            }

            Notification notification =
                    this.notificationFactory.toNotification(NotificationEvent.parseFrom(message.getBody()));

            if (Objects.isNull(notification)) {
                return Action.CommitMessage;
            }

            if (Objects.nonNull(notification.getId())) {
                this.notificationService.update(notification);
            } else {
                this.notificationService.insert(notification);
            }

            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted notification event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }

}