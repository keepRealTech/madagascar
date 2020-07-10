package com.keepreal.madagascar.marty.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.marty.model.PushType;
import com.keepreal.madagascar.marty.service.PushService;
import com.keepreal.madagascar.marty.service.RedissonService;
import com.keepreal.madagascar.marty.service.UmengPushService;
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
    private final RedissonService redissonService;

    public FeedEventListener(PushService pushService,
                             RedissonService redissonService) {
        this.pushService = pushService;
        this.redissonService = redissonService;
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
                pushService.pushNewFeed(islandId, PushType.PUSH_FEED);
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
