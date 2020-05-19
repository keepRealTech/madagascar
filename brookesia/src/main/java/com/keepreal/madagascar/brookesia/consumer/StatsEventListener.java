package com.keepreal.madagascar.brookesia.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.brookesia.StatsEventMessage;
import com.keepreal.madagascar.brookesia.factory.StatsEventFactory;
import com.keepreal.madagascar.brookesia.model.StatsEvent;
import com.keepreal.madagascar.brookesia.service.StatsEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the stats event listener.
 */
@Component
@Slf4j
public class StatsEventListener implements MessageListener {

    private final StatsEventService statsEventService;
    private final StatsEventFactory statsEventFactory;

    /**
     * Constructs the stats event listener.
     *
     * @param statsEventService {@link StatsEventService}.
     * @param statsEventFactory {@link StatsEventFactory}.
     */
    public StatsEventListener(StatsEventService statsEventService,
                              StatsEventFactory statsEventFactory) {
        this.statsEventService = statsEventService;
        this.statsEventFactory = statsEventFactory;
    }

    /**
     * Implements the stats event consumption logic.
     *
     * @param message Message in binary.
     * @param context Context.
     * @return {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }

            StatsEvent statsEvent =
                    this.statsEventFactory.valueOf(StatsEventMessage.parseFrom(message.getBody()));

            this.statsEventService.insert(statsEvent);

            return Action.CommitMessage;
        } catch (DuplicateKeyException exception) {
            log.warn("Duplicated consumption, skipped.");
            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted notification event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }
}
