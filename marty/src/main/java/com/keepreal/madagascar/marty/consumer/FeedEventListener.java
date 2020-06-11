package com.keepreal.madagascar.marty.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.marty.service.UmengPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.keepreal.madagascar.mantella.FeedEventType.FEED_EVENT_CREATE;

/**
 * Represents the feed event listener.
 */
@Component
@Slf4j
public class FeedEventListener implements MessageListener {

    private final UmengPushService umengPushService;

    public FeedEventListener(UmengPushService umengPushService) {
        this.umengPushService = umengPushService;
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
                String islandId = feedEventMessage.getFeedCreateEvent().getIslandId();
                umengPushService.pushFeed(islandId);
            }
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
