package com.keepreal.madagascar.mantella.factory;

import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.mantella.FeedEventType;
import com.keepreal.madagascar.mantella.model.Timeline;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the timeline factory.
 */
@Service
public class TimelineFactory {

    /**
     * Converts {@link FeedEventMessage} of {@link FeedCreateEvent} into {@link Timeline}.
     *
     * @param feedEventMessage {@link FeedEventMessage}.
     * @return {@link Timeline}.
     */
    public Timeline valueOf(FeedEventMessage feedEventMessage) {
         if (Objects.isNull(feedEventMessage)
                 || !FeedEventType.FEED_EVENT_CREATE.equals(feedEventMessage.getType())) {
             return null;
         }

         return Timeline.builder()
                 .feedId(feedEventMessage.getFeedCreateEvent().getFeedId())
                 .islandId(feedEventMessage.getFeedCreateEvent().getIslandId())
                 .fromHost(feedEventMessage.getFeedCreateEvent().getFromHost())
                 .feedCreatedAt(feedEventMessage.getFeedCreateEvent().getCreatedAt())
                 .build();
    }

}