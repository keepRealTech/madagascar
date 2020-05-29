package com.keepreal.madagascar.mantella.model;

import com.keepreal.madagascar.mantella.repository.TimelineRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents the timeline model.
 */
@Builder
@Data
@Document("timeline")
public class Timeline {

    @Id
    private String id;
    private String feedId;
    private String islandId;
    private Long feedCreatedAt;
    @Builder.Default
    private Boolean isFrozen = false;
    private String userId;

    private String eventId;
    @Builder.Default
    private Boolean isDeleted = false;
    @CreatedDate
    private Long createdAt;
    @LastModifiedDate
    private Long updatedAt;

}