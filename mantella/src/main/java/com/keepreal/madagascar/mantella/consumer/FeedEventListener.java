package com.keepreal.madagascar.mantella.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.mantella.factory.TimelineFactory;
import com.keepreal.madagascar.mantella.service.TimelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the feed event listener.
 */
@Component
@Slf4j
public class FeedEventListener implements MessageListener {

    private final TimelineService timelineService;

    /**
     * Constructs the feed event listener.
     *
     * @param timelineService {@link TimelineService}.
     */
    public FeedEventListener(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    /**
     * Implements the listener consume function logic.
     *
     * @param message {@link Message} Message queue message.
     * @param context {@link ConsumeContext} Consume context if applicable.
     * @return {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }

            Boolean hasConsumed = this.timelineService.hasConsumed(message.getKey()).block();

            if (Boolean.TRUE.equals(hasConsumed)) {
                return Action.CommitMessage;
            } else if (Objects.isNull(hasConsumed)) {
                return Action.ReconsumeLater;
            }

            FeedEventMessage feedEventMessage = FeedEventMessage.parseFrom(message.getBody());

            switch (feedEventMessage.getType()) {
                case FEED_EVENT_CREATE:
                    if (Objects.isNull(feedEventMessage.getFeedCreateEvent())) {
                        throw new InvalidProtocolBufferException("No feed event in message.");
                    }

                    this.timelineService.distribute(feedEventMessage).block();

                    return Action.CommitMessage;
                case FEED_EVENT_DELETE:
                    if (Objects.isNull(feedEventMessage.getFeedDeleteEvent())) {
                        throw new InvalidProtocolBufferException("No feed event in message.");
                    }

                    this.timelineService.delete(feedEventMessage.getFeedDeleteEvent().getFeedId()).block();
                    return Action.CommitMessage;
                default:
                    log.warn("No such feed event type {}, skipped.", feedEventMessage.getType());
                    return Action.CommitMessage;
            }

        } catch (DuplicateKeyException exception) {
            log.warn("Duplicated consumption, skipped.");
            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted feed event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }

}
