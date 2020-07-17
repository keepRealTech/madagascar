package com.keepreal.madagascar.marty.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.marty.model.PushType;
import com.keepreal.madagascar.marty.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.keepreal.madagascar.mantella.FeedEventType.FEED_EVENT_CREATE;

/**
 * Represents the feed event listener.
 */
@Component
@Slf4j
public class FeedEventListener implements MessageListener {

    private final PushService pushService;

    public FeedEventListener(PushService pushService) {
        this.pushService = pushService;
    }

    /**
     * Implements the message consume method.
     *
     * @param message   {@link Message}.
     * @param context   {@link ConsumeContext}.
     * @return  {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }
            FeedEventMessage feedEventMessage = FeedEventMessage.parseFrom(message.getBody());
            if (feedEventMessage.getType().equals(FEED_EVENT_CREATE)) {
                pushService.pushNewFeed(feedEventMessage.getFeedCreateEvent(), PushType.PUSH_FEED);
            }
            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted notification event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }
}
