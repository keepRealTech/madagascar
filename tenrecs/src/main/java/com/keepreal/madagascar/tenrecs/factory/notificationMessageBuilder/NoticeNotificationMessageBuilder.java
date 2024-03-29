package com.keepreal.madagascar.tenrecs.factory.notificationMessageBuilder;

import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.FeedPaymentNotice;
import com.keepreal.madagascar.tenrecs.NoticeNotificationMessage;
import com.keepreal.madagascar.tenrecs.NotificationMessage;
import com.keepreal.madagascar.tenrecs.MemberNotice;
import com.keepreal.madagascar.tenrecs.SubscribeNotice;
import com.keepreal.madagascar.tenrecs.factory.notificationBuilder.NotificationBuilder;
import com.keepreal.madagascar.tenrecs.model.Notice;
import com.keepreal.madagascar.tenrecs.model.Notification;

import java.util.Objects;

/**
 * Implements the {@link NotificationBuilder}.
 */
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
    @Override
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
                || !this.notification.getType().equals(NotificationType.NOTIFICATION_ISLAND_NOTICE)) {
            return null;
        }

        return NotificationMessage.newBuilder()
                .setId(this.notification.getId())
                .setType(NotificationType.NOTIFICATION_ISLAND_NOTICE)
                .setUserId(this.notification.getUserId())
                .setHasRead(this.notification.getCreatedAt().compareTo(this.lastReadTimestamp) < 0)
                .setNoticeNotification(this.toNoticeMessage(this.notification.getNotice()))
                .setTimestamp(this.notification.getTimestamp())
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
                    return noticeNotificationMessageBuilder.build();
                }

                SubscribeNotice subscribeNotice = SubscribeNotice.newBuilder()
                        .setIslandId(notice.getSubscribeNotice().getIslandId())
                        .setSubscriberId(notice.getSubscribeNotice().getSubscriberId())
                        .build();
                noticeNotificationMessageBuilder.setSubscribeNotice(subscribeNotice);
                return noticeNotificationMessageBuilder.build();
            case NOTICE_TYPE_ISLAND_NEW_MEMBER:
                if (Objects.isNull(notice.getMemberNotice())) {
                    return noticeNotificationMessageBuilder.build();
                }

                MemberNotice memberNotice = MemberNotice.newBuilder()
                        .setIslandId(notice.getMemberNotice().getIslandId())
                        .setMemberId(notice.getMemberNotice().getMemberId())
                        .setMembershipId(notice.getMemberNotice().getMembershipId())
                        .setMembershipName(notice.getMemberNotice().getMembershipName())
                        .setPriceInCents(notice.getMemberNotice().getPriceInCents())
                        .setTimeInMonths(notice.getMemberNotice().getTimeInMonths())
                        .setPermanent(notice.getMemberNotice().getPermanent() == null ? false : notice.getMemberNotice().getPermanent())
                        .build();
                noticeNotificationMessageBuilder.setMemberNotice(memberNotice);
                return noticeNotificationMessageBuilder.build();
            case NOTICE_TYPE_FEED_NEW_PAYMENT:
                if (Objects.isNull(notice.getFeedPaymentNotice())) {
                    return noticeNotificationMessageBuilder.build();
                }

                FeedPaymentNotice feedPaymentNotice = FeedPaymentNotice.newBuilder()
                        .setFeedId(notice.getFeedPaymentNotice().getFeedId())
                        .setUserId(notice.getFeedPaymentNotice().getUserId())
                        .setPriceInCents(notice.getFeedPaymentNotice().getPriceInCents())
                        .build();

                noticeNotificationMessageBuilder.setFeedPaymentNotice(feedPaymentNotice);
                return noticeNotificationMessageBuilder.build();
            default:
        }

        return noticeNotificationMessageBuilder.build();
    }

}
