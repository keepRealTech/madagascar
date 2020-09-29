package com.keepreal.madagascar.mantella.factory;

import com.keepreal.madagascar.mantella.TimelineMessage;
import com.keepreal.madagascar.mantella.model.Timeline;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the timeline message factory.
 */
@Service
public class TimelineMessageFactory {

    /**
     * Converts the {@link Timeline} into {@link TimelineMessage}.
     *
     * @param timeline {@link Timeline}.
     * @return {@link TimelineMessage}.
     */
    public TimelineMessage valueOf(Timeline timeline) {
        if (Objects.isNull(timeline)) {
            return null;
        }

        return TimelineMessage.newBuilder()
                .setFeedId(timeline.getFeedId())
                .setRecommendatedAt(timeline.getUpdatedAt())
                .build();
    }

}
