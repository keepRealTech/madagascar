package com.keepreal.madagascar.fossa.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.fossa.config.FeedEventProducerConfiguration;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.FeedDeleteEvent;
import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.mantella.FeedEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Represents the feed event producer service.
 */
@Service
@Slf4j
public class FeedEventProducerService {

    private final OrderProducerBean orderProducerBean;
    private final FeedEventProducerConfiguration feedEventProducerConfiguration;
    private final ExecutorService executorService;

    /**
     * Constructs the feed event producer service.
     *
     * @param orderProducerBean                      {@link OrderProducerBean}.
     * @param feedEventProducerConfiguration {@link FeedEventProducerConfiguration}.
     */
    public FeedEventProducerService(OrderProducerBean orderProducerBean,
                                    FeedEventProducerConfiguration feedEventProducerConfiguration) {
        this.orderProducerBean = orderProducerBean;
        this.feedEventProducerConfiguration = feedEventProducerConfiguration;
        this.executorService = new ThreadPoolExecutor(
                10,
                20,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500000),
                new CustomizableThreadFactory("feed-event-producer-"));
    }

    /**
     * Produces new feed message event into message queue.
     *
     * @param feedInfo {@link FeedInfo}.
     */
    public void produceNewFeedEventAsync(FeedInfo feedInfo) {
        Message message = this.createNewFeedEventMessage(feedInfo);
        this.sendAsync(message, feedInfo.getId());
    }

    /**
     * Produces new feed message event into message queue.
     *
     * @param feedId Feed id.
     */
    public void produceDeleteFeedEventAsync(String feedId) {
        Message message = this.createDeleteFeedEventMessage(feedId);
        this.sendAsync(message, feedId);
    }

    /**
     * Sends a message in async manner.
     *
     * @param message {@link Message}.
     */
    private void sendAsync(Message message, String shard) {
        if (Objects.isNull(message)) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> this.orderProducerBean.send(message, shard),
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
     * Creates the new feed event message.
     *
     * @param feedInfo {@link FeedMessage}.
     * @return {@link Message}.
     */
    private Message createNewFeedEventMessage(FeedInfo feedInfo) {
        if (Objects.isNull(feedInfo)) {
            return null;
        }

        FeedCreateEvent feedCreateEvent = FeedCreateEvent.newBuilder()
                .setFeedId(feedInfo.getId())
                .setAuthorId(feedInfo.getUserId())
                .setCreatedAt(feedInfo.getCreatedTime())
                .setIslandId(feedInfo.getIslandId())
                .setDuplicateTag(feedInfo.getDuplicateTag())
                .setFromHost(feedInfo.getFromHost())
                .build();
        String uuid = UUID.randomUUID().toString();
        FeedEventMessage event = FeedEventMessage.newBuilder()
                .setType(FeedEventType.FEED_EVENT_CREATE)
                .setFeedCreateEvent(feedCreateEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        return new Message(this.feedEventProducerConfiguration.getTopic(),
                this.feedEventProducerConfiguration.getTag(), uuid, event.toByteArray());
    }

    /**
     * Creates the delete feed event.
     *
     * @param feedId Feed id.
     * @return {@link Message}.
     */
    private Message createDeleteFeedEventMessage(String feedId) {
        if (StringUtils.isEmpty(feedId)) {
            return null;
        }

        FeedDeleteEvent feedDeleteEvent = FeedDeleteEvent.newBuilder()
                .setFeedId(feedId)
                .build();
        String uuid = UUID.randomUUID().toString();
        FeedEventMessage event = FeedEventMessage.newBuilder()
                .setType(FeedEventType.FEED_EVENT_DELETE)
                .setFeedDeleteEvent(feedDeleteEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        return new Message(this.feedEventProducerConfiguration.getTopic(),
                this.feedEventProducerConfiguration.getTag(), uuid, event.toByteArray());
    }

}