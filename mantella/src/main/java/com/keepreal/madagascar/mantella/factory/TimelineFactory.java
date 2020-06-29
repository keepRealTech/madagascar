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
     * @param userId          User id.
     * @param eventId         Event id.
     * @return {@link Timeline}.
     */
    public Timeline valueOf(FeedCreateEvent feedCreateEvent, String userId, String eventId) {
        if (Objects.isNull(feedCreateEvent)) {
            return null;
        }

        return Timeline.builder()
                .feedId(feedCreateEvent.getFeedId())
                .islandId(feedCreateEvent.getIslandId())
                .feedCreatedAt(feedCreateEvent.getCreatedAt())
                .userId(userId)
                .eventId(eventId)
                .build();
    }

    /**
     * Constructs the {@link Timeline}.
     *
     * @param feedId        Feed id.
     * @param islandId      Island id.
     * @param userId        User id.
     * @param feedCreatedAt Feed created at timestamp.
     * @param eventId       Event id.
     * @return {@link Timeline}.
     */
    public Timeline valueOf(String feedId, String islandId, String userId, Long feedCreatedAt, String eventId) {
        return Timeline.builder()
                .feedId(feedId)
                .islandId(islandId)
                .feedCreatedAt(feedCreatedAt)
                .userId(userId)
                .eventId(eventId)
                .build();
    }

}