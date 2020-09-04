package com.keepreal.madagascar.coua.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.coua.CreateUserEvent;
import com.keepreal.madagascar.coua.UserEvent;
import com.keepreal.madagascar.coua.UserEventType;
import com.keepreal.madagascar.coua.config.UserEventProducerConfiguration;
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
public class UserEventProducerService {

    private final ProducerBean producerBean;
    private final UserEventProducerConfiguration userEventProducerConfiguration;
    private final ExecutorService executorService;

    public UserEventProducerService(@Qualifier("user-event-producer") ProducerBean producerBean,
                                      UserEventProducerConfiguration userEventProducerConfiguration) {
        this.producerBean = producerBean;
        this.userEventProducerConfiguration = userEventProducerConfiguration;
        this.executorService = new ThreadPoolExecutor(
                10,
                20,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500000),
                new CustomizableThreadFactory("user-event-producer-"));
    }

    public void produceCreateUserEventAsync(String userId) {
        Message message = this.createUserEventMessage(userId);
        this.sendAsync(message);
    }

    private Message createUserEventMessage(String userId) {
        String uuid = UUID.randomUUID().toString();

        CreateUserEvent createIslandEvent = CreateUserEvent.newBuilder()
                .setUserId(userId)
                .build();

        UserEvent event = UserEvent.newBuilder()
                .setType(UserEventType.USER_EVENT_CREATE)
                .setEventId(uuid)
                .setTimestamp(System.currentTimeMillis())
                .setCreateUserEvent(createIslandEvent)
                .build();

        return new Message(this.userEventProducerConfiguration.getTopic(),
                this.userEventProducerConfiguration.getTag(), uuid, event.toByteArray());
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
