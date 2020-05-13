package com.keepreal.madagascar.brookesia.factory;

import com.keepreal.madagascar.brookesia.StatsEventMessage;
import com.keepreal.madagascar.brookesia.model.StatsEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the stats event factory.
 */
@Component
public class StatsEventFactory {

    /**
     * Converts {@link StatsEventMessage} into {@link StatsEvent}.
     *
     * @param event {@link StatsEventMessage}.
     * @return {@link StatsEvent}.
     */
    public StatsEvent toStatsEvent(StatsEventMessage event) {
        if (Objects.isNull(event)) {
            return null;
        }

        return StatsEvent.builder()
                .id(event.getEventId())
                .timestamp(event.getTimestamp())
                .category(event.getCategory())
                .action(event.getAction())
                .value(event.getValue())
                .label(event.getLabel())
                .succeed(event.getSucceed())
                .metadata(event.getMetadata())
                .build();
    }

}
