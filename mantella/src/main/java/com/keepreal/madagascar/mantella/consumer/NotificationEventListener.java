package com.keepreal.madagascar.mantella.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.keepreal.madagascar.mantella.factory.TimelineFactory;
import com.keepreal.madagascar.mantella.model.Timeline;
import com.keepreal.madagascar.mantella.service.FeedService;
import com.keepreal.madagascar.mantella.service.TimelineService;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Represents the notification event listener.
 */
@Component
@Slf4j
public class NotificationEventListener implements MessageListener {

    private final static int TIMELINE_PULL_PAGESIZE = 1000;
    private final FeedService feedService;
    private final TimelineService timelineService;
    private final TimelineFactory timelineFactory;

    /**
     * Constructs the notification event listener.
     *
     * @param feedService     {@link FeedService}.
     * @param timelineService {@link TimelineService}.
     * @param timelineFactory {@link TimelineFactory}.
     */
    public NotificationEventListener(FeedService feedService,
                                     TimelineService timelineService,
                                     TimelineFactory timelineFactory) {
        this.feedService = feedService;
        this.timelineService = timelineService;
        this.timelineFactory = timelineFactory;
    }

    /**
     * Implements the listener consume function logic.
     *
     * @param message {@link Message} Message queue message.
     * @param context {@link ConsumeOrderContext} Consume context if applicable.
     * @return {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        log.info("starting consuming a message.");
        try {
            NotificationEvent event = NotificationEvent.parseFrom(message.getBody());

            if (Objects.isNull(event)) {
                return Action.CommitMessage;
            }

            log.info(event.toString());

            switch (event.getType()) {
                case NOTIFICATION_EVENT_NEW_SUBSCRIBE:
                    log.info("dealing new subscription.");
                    if (Objects.isNull(event.getSubscribeEvent())
                        || StringUtils.isEmpty(event.getSubscribeEvent().getSubscriberId())) {
                        break;
                    }
                    this.feedService.retrieveFeedsByIslandIdAndTimestampBefore(event.getSubscribeEvent().getIslandId(),
                                    System.currentTimeMillis(), NotificationEventListener.TIMELINE_PULL_PAGESIZE)
                            .map(feed -> this.timelineFactory.valueOf(feed.getId(), feed.getIslandId(),
                                    event.getSubscribeEvent().getSubscriberId(), feed.getCreatedAt(), event.getEventId()))
                            .compose(this.timelineService::insertAll)
                            .blockLast();
                    break;
                case NOTIFICATION_EVENT_NEW_UNSUBSCRIBE:
                    log.info("dealing new unsubscription.");
                    if (Objects.isNull(event.getUnsubscribeEvent())
                            || StringUtils.isEmpty(event.getUnsubscribeEvent().getSubscriberId())) {
                        break;
                    }
                    this.timelineService.deleteByUserIdAndIslandId(event.getUnsubscribeEvent().getSubscriberId(), event.getUnsubscribeEvent().getIslandId())
                            .block();
                    break;
                default:
            }

            return Action.CommitMessage;
        } catch (Exception e) {
            log.error("Failed to commit new subscription event.");
            return Action.ReconsumeLater;
        }
    }

}