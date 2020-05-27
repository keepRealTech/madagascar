package com.keepreal.madagascar.mantella.factory;

import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.model.Timeline;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the timeline factory.
 */
@Service
public class TimelineFactory {

    /**
     * Converts {@link FeedCreateEvent} into {@link Timeline}.
     *
     * @param feedCreateEvent {@link FeedCreateEvent}.
     * @return {@link Timeline}.
     */
    public Timeline valueOf(FeedCreateEvent feedCreateEvent) {
         if (Objects.isNull(feedCreateEvent)) {
             return null;
         }

         return Timeline.builder()
                 .feedId(feedCreateEvent.getFeedId())
                 .islandId(feedCreateEvent.getIslandId())
                 .fromHost(feedCreateEvent.getFromHost())
                 .feedCreatedAt(feedCreateEvent.getCreatedAt())
                 .build();
    }

}