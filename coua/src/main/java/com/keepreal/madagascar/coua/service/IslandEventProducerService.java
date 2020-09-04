package com.keepreal.madagascar.coua.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.coua.CreateIslandEvent;
import com.keepreal.madagascar.coua.IslandEvent;
import com.keepreal.madagascar.coua.IslandEventType;
import com.keepreal.madagascar.coua.config.IslandEventProducerConfiguration;
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

@Service
@Slf4j
public class IslandEventProducerService {

    private final ProducerBean producerBean;
    private final IslandEventProducerConfiguration islandEventProducerConfiguration;
    private final ExecutorService executorService;

    public IslandEventProducerService(@Qualifier("island-event-producer") ProducerBean producerBean,
                                      IslandEventProducerConfiguration islandEventProducerConfiguration) {
        this.producerBean = producerBean;
        this.islandEventProducerConfiguration = islandEventProducerConfiguration;
        this.executorService = new ThreadPoolExecutor(
                10,
                20,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500000),
                new CustomizableThreadFactory("island-event-producer-"));
    }

    public void produceCreateIslandEventAsync(String hostId) {
        Message message = this.createIslandEventMessage(hostId);
        this.sendAsync(message);
    }

    private Message createIslandEventMessage(String hostId) {
        String uuid = UUID.randomUUID().toString();

        CreateIslandEvent createIslandEvent = CreateIslandEvent.newBuilder()
                .setHostId(hostId)
                .build();

        IslandEvent event = IslandEvent.newBuilder()
                .setType(IslandEventType.ISLAND_EVENT_CREATE)
                .setEventId(uuid)
                .setTimestamp(System.currentTimeMillis())
                .setCreateIslandEvent(createIslandEvent)
                .build();

        return new Message(this.islandEventProducerConfiguration.getTopic(),
                this.islandEventProducerConfiguration.getTag(), uuid, event.toByteArray());
    }

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
}
