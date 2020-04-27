package com.keepreal.madagascar.lemur.dtoFactory.notification;

import com.keepreal.madagascar.common.IslandNoticeMessage;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import swagger.model.IslandNoticeDTO;
import swagger.model.NotificationDTO;
import swagger.model.NotificationType;

import java.util.Objects;

/**
 * Represents the island notice notification dto builder.
 */
public class IslandNoticeNotificationDTOBuilder implements NotificationDTOBuilder {

    private NotificationMessage notificationMessage;

    /**
     * Sets the notification message.
     *
     * @param notificationMessage {@link NotificationMessage}.
     * @return {@link IslandNoticeNotificationDTOBuilder}.
     */
    @Override
    public IslandNoticeNotificationDTOBuilder setNotificationMessage(NotificationMessage notificationMessage) {
        this.notificationMessage = notificationMessage;
        return this;
    }

    /**
     * Builds the {@link NotificationDTO}.
     *
     * @return {@link NotificationDTO}.
     */
    @Override
    public NotificationDTO build() {
        if (Objects.isNull(this.notificationMessage)) {
            return null;
        }

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(this.notificationMessage.getId());
        notificationDTO.setHasRead(this.notificationMessage.getHasRead());
        notificationDTO.setNotificationType(NotificationType.ISLAND_NOTICE);
        notificationDTO.setCreatedAt(this.notificationMessage.getCreatedAt());
        notificationDTO.setIslandNotice(this.valueOf(this.notificationMessage.getIslandNotice()));

        return notificationDTO;
    }

    /**
     * Converts the {@link IslandNoticeMessage} into {@link IslandNoticeDTO}.
     *
     * @param islandNotice {@link IslandNoticeMessage}.
     * @return {@link IslandNoticeDTO}.
     */
    private IslandNoticeDTO valueOf(IslandNoticeMessage islandNotice) {
        if (Objects.isNull(islandNotice)) {
            return null;
        }

        IslandNoticeDTO islandNoticeDTO = new IslandNoticeDTO();
        islandNoticeDTO.setIslandId(islandNotice.getIslandId());
        islandNoticeDTO.setContent(islandNotice.getContent());

        return islandNoticeDTO;
    }

}
