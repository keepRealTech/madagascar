package com.keepreal.madagascar.fossa.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.fossa.config.NotificationEventProducerConfiguration;
import com.keepreal.madagascar.tenrecs.CommentEvent;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import com.keepreal.madagascar.tenrecs.ReactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
     * Produces new comment message event into message queue.
     *
     * @param commentMessage {@link CommentMessage}.
     * @param feedMessage    {@link FeedMessage}.
     * @param replyToId      String reply to user id.
     */
    public void produceNewCommentsNotificationEventAsync(CommentMessage commentMessage, FeedMessage feedMessage, String replyToId) {
        Message message = this.createNewCommentEventMessage(commentMessage, feedMessage, feedMessage.getUserId());
        this.sendAsync(message);
        if (!StringUtils.isEmpty(replyToId) && !replyToId.equals(feedMessage.getUserId())) {
            message = this.createNewCommentEventMessage(commentMessage, feedMessage, replyToId);
            this.sendAsync(message);
        }
    }

    /**
     * Produces new comment message event into message queue.
     *
     * @param reactionMessage {@link ReactionMessage}.
     * @param feedMessage     {@link FeedMessage}.
     */
    public void produceNewReactionsNotificationEventAsync(ReactionMessage reactionMessage, FeedMessage feedMessage) {
        Message message = this.createNewReactionEventMessage(reactionMessage, feedMessage);
        this.sendAsync(message);
    }

    /**
     * Sends a message in async manner.
     *
     * @param message {@link Message}.
     */
    private void sendAsync(Message message) {
        CompletableFuture
                .supplyAsync(() -> this.producerBean.send(message),
                        this.executorService)
                .exceptionally(throwable -> {
                    log.warn(throwable.getMessage());
                    return null;
                });
    }

    /**
     * Creates a new comment event message.
     *
     * @param commentMessage {@link CommentMessage}.
     * @param feedMessage    {@link FeedMessage}.
     * @param userId         receiver id.
     * @return {@link Message}.
     */
    private Message createNewCommentEventMessage(CommentMessage commentMessage, FeedMessage feedMessage, String userId) {
        CommentEvent commentEvent = CommentEvent.newBuilder()
                .setComment(commentMessage)
                .setFeed(feedMessage)
                .build();
        String uuid = UUID.randomUUID().toString();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_COMMENT)
                .setUserId(userId)
                .setCommentEvent(commentEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        return new Message(this.notificationEventProducerConfiguration.getTopic(),
                this.notificationEventProducerConfiguration.getTag(), uuid, event.toByteArray());
    }

    /**
     * Creates a new reaction event message.
     *
     * @param reactionMessage {@link ReactionMessage}.
     * @param feedMessage     {@link FeedMessage}.
     * @return {@link Message}.
     */
    private Message createNewReactionEventMessage(ReactionMessage reactionMessage, FeedMessage feedMessage) {
        ReactionEvent reactionEvent = ReactionEvent.newBuilder()
                .setReaction(reactionMessage)
                .setFeed(feedMessage)
                .build();
        String uuid = UUID.randomUUID().toString();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_REACTION)
                .setUserId(feedMessage.getUserId())
                .setReactionEvent(reactionEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        return new Message(this.notificationEventProducerConfiguration.getTopic(),
                this.notificationEventProducerConfiguration.getTag(), uuid, event.toByteArray());
    }

}
