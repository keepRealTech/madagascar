package com.keepreal.madagascar.mantella.factory;

import com.keepreal.madagascar.mantella.FeedCreateEvent;
import com.keepreal.madagascar.mantella.model.Timeline;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

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

        // feed去重逻辑 根据duplicateTag分组只取一条 但是发送feed本人要都显示
        String duplicateTag = feedCreateEvent.getDuplicateTag();
        if (userId.equals(feedCreateEvent.getAuthorId())) {
            duplicateTag = UUID.randomUUID().toString();
        }

        return Timeline.builder()
                .feedId(feedCreateEvent.getFeedId())
                .islandId(feedCreateEvent.getIslandId())
                .feedCreatedAt(feedCreateEvent.getCreatedAt())
                .userId(userId)
                .duplicateTag(duplicateTag)
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