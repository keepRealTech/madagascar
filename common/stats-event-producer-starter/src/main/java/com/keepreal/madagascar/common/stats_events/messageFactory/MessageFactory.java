package com.keepreal.madagascar.common.stats_events.messageFactory;

import com.aliyun.openservices.ons.api.Message;
import com.keepreal.madagascar.brookesia.StatsEventMessage;
import com.keepreal.madagascar.common.stats_events.config.StatsEventProducerConfiguration;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a message factory.
 */
@Component
public class MessageFactory {

    private final StatsEventProducerConfiguration statsEventProducerConfiguration;

    /**
     * Constructs the {@link MessageFactory}.
     * @param statsEventProducerConfiguration {@link StatsEventProducerConfiguration}.
     */
    public MessageFactory(StatsEventProducerConfiguration statsEventProducerConfiguration) {
        this.statsEventProducerConfiguration = statsEventProducerConfiguration;
    }

    /**
     * Converts the {@link StatsEventMessage} into {@link Message}.
     * @param statsEventMessageBuilder {@link StatsEventMessage.Builder}.
     * @return {@link Message}.
     */
    public Message valueOf(StatsEventMessage.Builder statsEventMessageBuilder) {
        String uuid = UUID.randomUUID().toString();
        statsEventMessageBuilder
                .setEventId(uuid)
                .setTimestamp(Instant.now().toEpochMilli());
        Message message = new Message(
                this.statsEventProducerConfiguration.getTopic(),
                this.statsEventProducerConfiguration.getTag(),
                statsEventMessageBuilder.build().toByteArray());
        message.setKey(uuid);
        return message;
    }

}