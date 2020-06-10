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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Represents the notification event listener.
 */
@Component
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
        try {
            NotificationEvent event = NotificationEvent.parseFrom(message.getBody());

            if (Objects.isNull(event)
                    || !NotificationEventType.NOTIFICATION_EVENT_NEW_SUBSCRIBE.equals(event.getType())
                    || Objects.isNull(event.getSubscribeEvent())
                    || StringUtils.isEmpty(event.getSubscribeEvent().getSubscriberId())) {
                return Action.CommitMessage;
            }

            this.timelineService.retrieveLastFeedTimestampByUserId(event.getSubscribeEvent().getSubscriberId())
                    .map(Timeline::getFeedCreatedAt)
                    .switchIfEmpty(Mono.just(0L))
                    .flatMapMany(timestamp -> this.feedService.retrieveFeedsByIslandIdAnd(event.getSubscribeEvent().getIslandId(),
                            timestamp, NotificationEventListener.TIMELINE_PULL_PAGESIZE))
                    .map(feed -> this.timelineFactory.valueOf(feed.getId(), feed.getIslandId(),
                            feed.getUserId(), feed.getCreatedAt(), event.getEventId()))
                    .compose(this.timelineService::insertAll)
                    .blockLast();

            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }

}