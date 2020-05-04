package com.keepreal.madagascar.lemur.dtoFactory.notificationBuilder;

import com.keepreal.madagascar.tenrecs.NotificationMessage;
import swagger.model.NotificationDTO;

/**
 * Represents the interface for notification dto builder.
 */
public interface NotificationDTOBuilder {

    NotificationDTO build();

    NotificationDTOBuilder setNotificationMessage(NotificationMessage notificationMessage);

}
