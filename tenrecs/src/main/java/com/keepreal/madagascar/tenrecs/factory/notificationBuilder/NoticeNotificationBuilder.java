package com.keepreal.madagascar.tenrecs.factory.notificationBuilder;

import com.keepreal.madagascar.common.NoticeType;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import com.keepreal.madagascar.tenrecs.model.Notice;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.model.notice.FeedPaymentNotice;
import com.keepreal.madagascar.tenrecs.model.notice.MemberNotice;
import com.keepreal.madagascar.tenrecs.model.notice.SubscribeNotice;
import com.keepreal.madagascar.tenrecs.service.NotificationService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

/**
 * Implements the {@link NotificationBuilder}.
 */
public class NoticeNotificationBuilder implements NotificationBuilder {

    private NotificationEvent event;
    private NotificationService notificationService;

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
     * Sets the notification service.
     *
     * @param notificationService {@link NotificationService}.
     * @return this.
     */
    public NoticeNotificationBuilder setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        return this;
    }

    /**
     * Builds the {@link Notification}.
     *
     * @return {@link Notification}.
     */
    @Override
    public Notification build() {
        if (Objects.isNull(this.event)
                || Objects.isNull(this.notificationService)) {
            return null;
        }

        if (NotificationEventType.NOTIFICATION_EVENT_NEW_SUBSCRIBE.equals(this.event.getType())) {
            Optional<Notification> lastNotification = this.notificationService.retrieveLastSubscribeNoticeByIslandIdAndSubscriberId(
                    this.event.getSubscribeEvent().getIslandId(), this.event.getSubscribeEvent().getSubscriberId());
            if (lastNotification.isPresent()
                    && lastNotification.get().getTimestamp() > LocalDateTime.now().minusDays(1).atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()) {
                Notification ln = lastNotification.get();
                ln.setTimestamp(this.event.getTimestamp());
                return ln;
            }
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
            case NOTIFICATION_EVENT_NEW_MEMBER:
                noticeBuilder.type(NoticeType.NOTICE_TYPE_ISLAND_NEW_MEMBER);

                if (Objects.isNull(this.event.getMemberEvent())) {
                    return noticeBuilder.build();
                }

                MemberNotice memberNotice = MemberNotice.builder()
                        .islandId(this.event.getMemberEvent().getIslandId())
                        .memberId(this.event.getMemberEvent().getMemberId())
                        .membershipId(this.event.getMemberEvent().getMembershipId())
                        .membershipName(this.event.getMemberEvent().getMembershipName())
                        .priceInCents(this.event.getMemberEvent().getPriceInCents())
                        .timeInMonths(this.event.getMemberEvent().getTimeInMonths())
                        .permanent(this.event.getMemberEvent().getPermanent())
                        .build();
                noticeBuilder.memberNotice(memberNotice);

                return noticeBuilder.build();
            case NOTIFICATION_EVENT_NEW_SUPPORT:
                noticeBuilder.type(NoticeType.NOTICE_TYPE_ISLAND_NEW_MEMBER);

                if (Objects.isNull(this.event.getMemberEvent())) {
                    return noticeBuilder.build();
                }

                MemberNotice support = MemberNotice.builder()
                        .islandId("")
                        .memberId(this.event.getSupportEvent().getUserId())
                        .membershipId("")
                        .membershipName("")
                        .priceInCents(this.event.getSupportEvent().getPriceInCents())
                        .timeInMonths(0)
                        .build();
                noticeBuilder.memberNotice(support);

                return noticeBuilder.build();
            case NOTIFICATION_EVENT_NEW_FEED_PAYMENT:
                noticeBuilder.type(NoticeType.NOTICE_TYPE_FEED_NEW_PAYMENT);

                if (Objects.isNull(this.event.getFeedPaymentEvent())) {
                    return noticeBuilder.build();
                }

                FeedPaymentNotice feedPaymentNotice = FeedPaymentNotice.builder()
                        .userId(this.event.getFeedPaymentEvent().getUserId())
                        .feedId(this.event.getFeedPaymentEvent().getFeedId())
                        .priceInCents(this.event.getFeedPaymentEvent().getPriceInCents())
                        .build();
                noticeBuilder.feedPaymentNotice(feedPaymentNotice);

                return noticeBuilder.build();
            default:
        }

        return noticeBuilder.build();
    }

}