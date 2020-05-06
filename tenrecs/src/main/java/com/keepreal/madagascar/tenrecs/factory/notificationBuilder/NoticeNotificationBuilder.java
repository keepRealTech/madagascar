package com.keepreal.madagascar.tenrecs.factory.notificationBuilder;

import com.keepreal.madagascar.common.NoticeType;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.model.Notice;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.model.notice.SubscribeNotice;

import java.util.Objects;

/**
 * Implements the {@link NotificationBuilder}.
 */
public class NoticeNotificationBuilder implements NotificationBuilder {

    private NotificationEvent event;

    /**
     * Sets the notification event.
     *
     * @param event {@link NotificationEvent}.
     * @return this.
     */
    @Override
    public NoticeNotificationBuilder setEvent(NotificationEvent event) {
        this.event = event;
        return this;
    }

    /**
     * Builds the {@link Notification}.
     *
     * @return {@link Notification}.
     */
    @Override
    public Notification build() {
        if (Objects.isNull(this.event)) {
            return null;
        }

        return Notification.builder()
                .type(NotificationType.NOTIFICATION_ISLAND_NOTICE)
                .userId(this.event.getUserId())
                .eventId(this.event.getEventId())
                .timestamp(this.event.getTimestamp())
                .notice(this.buildNotice())
                .build();
    }

    /**
     * Builds the {@link Notice}.
     *
     * @return {@link Notice}.
     */
    private Notice buildNotice() {
        Notice.NoticeBuilder noticeBuilder = Notice.builder();

        switch (this.event.getType()) {
            case NOTIFICATION_EVENT_NEW_SUBSCRIBE:
                noticeBuilder.type(NoticeType.NOTICE_TYPE_ISLAND_NEW_SUBSCRIBER);

                if (Objects.isNull(this.event.getSubscribeEvent())) {
                    return noticeBuilder.build();
                }

                SubscribeNotice subscribeNotice = SubscribeNotice.builder()
                        .islandId(this.event.getSubscribeEvent().getIslandId())
                        .subscriberId(this.event.getSubscribeEvent().getSubscriberId())
                        .build();
                noticeBuilder.subscribeNotice(subscribeNotice);

                return noticeBuilder.build();
            default:
        }

        return noticeBuilder.build();
    }

}
