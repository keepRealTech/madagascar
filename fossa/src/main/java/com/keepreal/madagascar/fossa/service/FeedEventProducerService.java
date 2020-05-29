package com.keepreal.madagascar.fossa.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import com.keepreal.madagascar.fossa.config.NotificationEventProducerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

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
    private final NotificationEventProducerConfiguration notificationEventProducerConfiguration;
    private final ExecutorService executorService;

    /**
     * Constructs the feed event producer service.
     * @param orderProducerBean {@link OrderProducerBean}.
     * @param notificationEventProducerConfiguration {@link NotificationEventProducerConfiguration}.
     */
    public FeedEventProducerService(OrderProducerBean orderProducerBean,
                                    NotificationEventProducerConfiguration notificationEventProducerConfiguration) {
        this.orderProducerBean = orderProducerBean;
        this.notificationEventProducerConfiguration = notificationEventProducerConfiguration;
        this.executorService = new ThreadPoolExecutor(
                10,
                20,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500000),
                new CustomizableThreadFactory("feed-event-producer-"));
    }

    /**
     * Sends a message in async manner.
     *
     * @param message {@link Message}.
     */
    private void sendAsync(Message message, String shard) {
        CompletableFuture
                .supplyAsync(() -> this.orderProducerBean.send(message, shard),
                        this.executorService)
                .handle((re, thr) -> {

                })
                .exceptionally(throwable -> {
                    log.warn(throwable.getMessage());
                    return null;
                });
    }

}
