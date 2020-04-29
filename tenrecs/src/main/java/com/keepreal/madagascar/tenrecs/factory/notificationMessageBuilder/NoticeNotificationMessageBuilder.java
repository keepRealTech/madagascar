package com.keepreal.madagascar.tenrecs.factory.notificationMessageBuilder;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.NoticeNotificationMessage;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.SubscribeNotice;
import com.keepreal.madagascar.tenrecs.model.Notice;
import com.keepreal.madagascar.tenrecs.model.Notification;

import java.util.Objects;

public class NoticeNotificationMessageBuilder implements NotificationMessageBuilder {

    private long lastReadTimestamp;
    private Notification notification;

    /**
     * Sets the last read timestamp.
     *
     * @param lastReadTimestamp Last read comment notification timestamp.
     * @return this.
     */
    public NoticeNotificationMessageBuilder setLastReadTimestamp(long lastReadTimestamp) {
        this.lastReadTimestamp = lastReadTimestamp;
        return this;
    }

    /**
     * Sets the notification.
     *
     * @param notification {@link Notification}.
     * @return this.
     */
    public NoticeNotificationMessageBuilder setNotification(Notification notification) {
        this.notification = notification;
        return this;
    }

    /**
     * Builds the {@link NotificationMessage}.
     *
     * @return {@link NotificationMessage}.
     */
    @Override
    public NotificationMessage build() {
        if (Objects.isNull(this.notification)
                || !notification.getType().equals(NotificationType.NOTIFICATION_ISLAND_NOTICE)) {
            return null;
        }

        return NotificationMessage.newBuilder()
                .setId(String.valueOf(this.notification.getId()))
                .setType(NotificationType.NOTIFICATION_ISLAND_NOTICE)
                .setUserId(this.notification.getUserId())
                .setHasRead(this.notification.getCreatedAt().compareTo(this.lastReadTimestamp) < 0)
                .setNoticeNotification(this.toNoticeMessage(this.notification.getNotice()))
                .setCreatedAt(this.notification.getCreatedAt())
                .build();
    }

    /**
     * Converts {@link Notice} into {@link NoticeNotificationMessage}.
     *
     * @param notice {@link Notice}.
     * @return {@link NoticeNotificationMessage}.
     */
    private NoticeNotificationMessage toNoticeMessage(Notice notice) {
        if (Objects.isNull(notice)) {
            return null;
        }

        NoticeNotificationMessage.Builder noticeNotificationMessageBuilder = NoticeNotificationMessage.newBuilder()
                .setType(notice.getType());

        switch (notice.getType()) {
            case NOTICE_TYPE_ISLAND_NEW_SUBSCRIBER:
                if (Objects.isNull(notice.getSubscribeNotice())) {
                    break;
                }

                noticeNotificationMessageBuilder.setSubscribeNotice(SubscribeNotice.newBuilder()
                        .setIslandId(notice.getSubscribeNotice().getIslandId())
                        .setSubscriberId(notice.getSubscribeNotice().getSubscriberId())
                        .build());
                break;
            default:
        }

        return noticeNotificationMessageBuilder.build();
    }

}
