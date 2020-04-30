package com.keepreal.madagascar.tenrecs.factory.notificationBuilder;

import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.model.Notification;

/**
 * Represents the notification builder interface.
 */
public interface NotificationBuilder {

    NotificationBuilder setEvent(NotificationEvent event);

    Notification build();

}
