package com.keepreal.madagascar.brookesia.model;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents the stats event model.
 */
@Builder
@Data
@Document("stats-event")
public class StatsEvent {

    @Id
    private String id;
    private Long timestamp;
    private StatsEventCategory category;
    private StatsEventAction action;
    private String label;
    private String value;
    private Boolean succeed;
    private String metadata;

}