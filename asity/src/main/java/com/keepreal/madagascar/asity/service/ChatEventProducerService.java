package com.keepreal.madagascar.asity.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.asity.UpdateBulletinEvent;
import com.keepreal.madagascar.asity.config.ChatEventProducerConfiguration;
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
public class ChatEventProducerService {

    private final ProducerBean producerBean;
    private final ChatEventProducerConfiguration chatEventProducerConfiguration;
    private final ExecutorService executorService;

    public ChatEventProducerService(@Qualifier("chat-event-producer") ProducerBean producerBean,
                                    ChatEventProducerConfiguration chatEventProducerConfiguration) {
        this.producerBean = producerBean;
        this.chatEventProducerConfiguration = chatEventProducerConfiguration;
        this.executorService = new ThreadPoolExecutor(
                10,
                20,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500000),
                new CustomizableThreadFactory("chat-event-producer-"));
    }

    public void produceUpdateBulletinChatEventAsync(String chatGroupId, String userId, String bulletin) {
        Message message = this.createUpdateBulletinEventMessage(chatGroupId, userId, bulletin);
        this.sendAsync(message);
    }

    private Message createUpdateBulletinEventMessage(String chatGroupId, String userId, String bulletin) {
        String uuid = UUID.randomUUID().toString();

        UpdateBulletinEvent event = UpdateBulletinEvent.newBuilder()
                .setChatGroupId(chatGroupId)
                .setUserId(userId)
                .setBulletin(bulletin)
                .build();

        return new Message(this.chatEventProducerConfiguration.getTopic(),
                this.chatEventProducerConfiguration.getTag(), uuid, event.toByteArray());
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
}
