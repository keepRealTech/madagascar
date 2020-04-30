package com.keepreal.madagascar.tenrecs.factory;

import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.factory.notificationBuilder.CommentNotificationBuilder;
import com.keepreal.madagascar.tenrecs.factory.notificationBuilder.NoticeNotificationBuilder;
import com.keepreal.madagascar.tenrecs.factory.notificationBuilder.ReactionNotificationBuilder;
import com.keepreal.madagascar.tenrecs.model.Notification;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the notification factory.
 */
@Component
public class NotificationFactory {

    /**
     * Converts {@link NotificationEvent} into {@link Notification}.
     *
     * @param event {@link NotificationEvent}.
     * @return {@link Notification}.
     */
    public Notification toNotification(NotificationEvent event) {
        if (Objects.isNull(event)) {
            return null;
        }

        switch (event.getType()) {
            case NOTIFICATION_EVENT_NEW_COMMENT:
                return new CommentNotificationBuilder().setEvent(event).build();
            case NOTIFICATION_EVENT_NEW_REACTION:
                return new ReactionNotificationBuilder().setEvent(event).build();
            case NOTIFICATION_EVENT_NEW_SUBSCRIBE:
                return new NoticeNotificationBuilder().setEvent(event).build();
            default:
                return null;
        }
    }

}
