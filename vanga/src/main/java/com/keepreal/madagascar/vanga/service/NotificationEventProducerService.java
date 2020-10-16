package com.keepreal.madagascar.vanga.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.tenrecs.BalanceEvent;
import com.keepreal.madagascar.tenrecs.MemberEvent;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import com.keepreal.madagascar.tenrecs.SupportEvent;
import com.keepreal.madagascar.vanga.config.NotificationEventProducerConfiguration;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.SubscribeMembership;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Represents the notification event producer.
 */
@Service
@Slf4j
public class NotificationEventProducerService {

    private final ProducerBean producerBean;
    private final NotificationEventProducerConfiguration notificationEventProducerConfiguration;
    private final ExecutorService executorService;

    /**
     * Constructs the notification event producer service.
     *
     * @param producerBean                           Notification event producer bean.
     * @param notificationEventProducerConfiguration {@link NotificationEventProducerConfiguration}.
     */
    public NotificationEventProducerService(@Qualifier("notification-event-producer") ProducerBean producerBean,
                                            NotificationEventProducerConfiguration notificationEventProducerConfiguration) {
        this.producerBean = producerBean;
        this.notificationEventProducerConfiguration = notificationEventProducerConfiguration;
        this.executorService = new ThreadPoolExecutor(
                10,
                20,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500000),
                new CustomizableThreadFactory("notification-event-producer-"));
    }

    /**
     * Produces new member event into message queue.
     *
     * @param subscribeMembership {@link SubscribeMembership}.
     */
    public void produceNewMemberNotificationEventAsync(SubscribeMembership subscribeMembership, MembershipSku membershipSku) {
        Message message = this.createNewMemberEventMessage(subscribeMembership, membershipSku);
        this.sendAsync(message);
    }

    public void produceNewSupportNotificationEventAsync(String userId, String payeeId, Long priceInCents) {
        Message message = this.createNewSupportEventMessage(userId, payeeId, priceInCents);
        this.sendAsync(message);
    }

    public void produceNewBalanceNotificationEventAsync(String hostId, Long amountInCents) {
        Message message = this.createNewBalanceEventMessage(hostId, amountInCents);
        this.sendAsync(message);
    }

    /**
     * Sends a message in async manner.
     *
     * @param message {@link Message}.
     */
    private void sendAsync(Message message) {
        if (Objects.isNull(message)) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> this.producerBean.send(message),
                        this.executorService)
                .handle((re, thr) -> {
                    if (Objects.nonNull(re)) {
                        return re;
                    }
                    log.warn("Sending message to topic {} error.", message.getTopic());
                    return null;
                });
    }

    /**
     * Creates new member event message.
     *
     * @param subscribeMembership {@link SubscribeMembership}.
     * @param membershipSku       {@link MembershipSku}.
     * @return {@link Message}.
     */
    private Message createNewMemberEventMessage(SubscribeMembership subscribeMembership, MembershipSku membershipSku) {
        MemberEvent memberEvent = MemberEvent.newBuilder()
                .setIslandId(subscribeMembership.getIslandId())
                .setMemberId(subscribeMembership.getUserId())
                .setMembershipId(subscribeMembership.getMembershipId())
                .setMembershipName(membershipSku.getMembershipName())
                .setPriceInCents(membershipSku.getPriceInCents())
                .setTimeInMonths(membershipSku.getTimeInMonths())
                .setPermanent(membershipSku.getPermanent())
                .build();

        String uuid = UUID.randomUUID().toString();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_MEMBER)
                .setUserId(membershipSku.getHostId())
                .setMemberEvent(memberEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        return new Message(this.notificationEventProducerConfiguration.getTopic(),
                this.notificationEventProducerConfiguration.getTag(), uuid, event.toByteArray());
    }

    private Message createNewSupportEventMessage(String userId, String payeeId, Long priceInCents) {
        SupportEvent supportEvent = SupportEvent.newBuilder()
                .setUserId(userId)
                .setPriceInCents(priceInCents)
                .build();

        String uuid = UUID.randomUUID().toString();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_SUPPORT)
                .setUserId(payeeId)
                .setSupportEvent(supportEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();

        return new Message(this.notificationEventProducerConfiguration.getTopic(),
                this.notificationEventProducerConfiguration.getTag(), uuid, event.toByteArray());
    }

    private Message createNewBalanceEventMessage(String hostId, Long amountInCents) {
        BalanceEvent balanceEvent = BalanceEvent.newBuilder()
                .setHostId(hostId)
                .setAmountInCents(amountInCents)
                .build();

        String uuid = UUID.randomUUID().toString();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_BALANCE)
                .setUserId(hostId)
                .setBalanceEvent(balanceEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();

        return new Message(this.notificationEventProducerConfiguration.getTopic(),
                this.notificationEventProducerConfiguration.getTag(), uuid, event.toByteArray());
    }
}
