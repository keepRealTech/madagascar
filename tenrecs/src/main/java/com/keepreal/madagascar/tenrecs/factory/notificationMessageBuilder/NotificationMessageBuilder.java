package com.keepreal.madagascar.tenrecs.factory.notificationMessageBuilder;

import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.model.Notification;

/**
 * Represents the notification message builder interface.
 */
public interface NotificationMessageBuilder {

    NotificationMessageBuilder setNotification(Notification notification);

    NotificationMessage build();

}
