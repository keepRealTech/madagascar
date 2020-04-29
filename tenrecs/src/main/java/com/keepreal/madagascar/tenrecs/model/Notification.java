package com.keepreal.madagascar.tenrecs.model;

import com.keepreal.madagascar.common.NotificationType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents the notification model.
 */
@Builder
@Data
@Document("notification")
public class Notification {

    @Id
    private long id;
    private String userId;

    private NotificationType type;

    private Feed feed;
    private Comment comment;
    private Reaction reaction;
    private IslandNotice notice;

    @Builder.Default
    private Boolean isDeleted = false;
    @CreatedDate
    private Long createdAt;
    @LastModifiedDate
    private Long updatedAt;

}
