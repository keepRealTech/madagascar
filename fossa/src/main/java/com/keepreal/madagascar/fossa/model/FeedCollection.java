package com.keepreal.madagascar.fossa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EntityListeners;

/**
 * Represents the feed collection data entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "feedCollection")
@EntityListeners(AuditingEntityListener.class)
public class FeedCollection {

    private String id;
    private String userId;
    private String feedId;
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
