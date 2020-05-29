package com.keepreal.madagascar.mantella.consumer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.mantella.FeedEventMessage;
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
public class FeedEventListener implements MessageOrderListener {

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
     * @param context {@link ConsumeOrderContext} Consume context if applicable.
     * @return {@link OrderAction}.
     */
    @Override
    public OrderAction consume(Message message, ConsumeOrderContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return OrderAction.Success;
            }

            FeedEventMessage feedEventMessage = FeedEventMessage.parseFrom(message.getBody());

            switch (feedEventMessage.getType()) {
                case FEED_EVENT_CREATE:
                    Boolean hasConsumed = this.timelineService.hasConsumed(message.getKey()).block();

                    if (Boolean.TRUE.equals(hasConsumed)) {
                        return OrderAction.Success;
                    } else if (Objects.isNull(hasConsumed)) {
                        return OrderAction.Suspend;
                    }

                    if (Objects.isNull(feedEventMessage.getFeedCreateEvent())) {
                        throw new InvalidProtocolBufferException("No feed event in message.");
                    }

                    this.timelineService.distribute(feedEventMessage.getFeedCreateEvent(), feedEventMessage.getEventId()).block();

                    return OrderAction.Success;
                case FEED_EVENT_DELETE:
                    if (Objects.isNull(feedEventMessage.getFeedDeleteEvent())) {
                        throw new InvalidProtocolBufferException("No feed event in message.");
                    }

                    this.timelineService.deleteByFeedId(feedEventMessage.getFeedDeleteEvent().getFeedId()).block();
                    return OrderAction.Success;
                default:
                    log.warn("No such feed event type {}, skipped.", feedEventMessage.getType());
                    return OrderAction.Success;
            }

        } catch (DuplicateKeyException exception) {
            log.warn("Duplicated consumption, skipped.");
            return OrderAction.Success;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted feed event, skipped.");
            return OrderAction.Success;
        } catch (Exception e) {
            return OrderAction.Suspend;
        }
    }

}
