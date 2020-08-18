package com.keepreal.madagascar.fossa.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EntityListeners;
import java.util.List;

/**
 * Represents the feed group data entity.
 */
@Data
@Builder
@Document(value = "feedGroup")
@EntityListeners(AuditingEntityListener.class)
public class FeedGroup {

    @Id
    private String id;
    private String islandId;
    private String hostId;
    private List<String> imageUrls;
    private List<String> feedIds;
    private Long lastFeedTime;
    private String thumbnailUri;
    @Builder.Default
    private Integer likesCount = 0;
    @Builder.Default
    private Integer commentsCount = 0;
    @Builder.Default
    private Integer repostCount = 0;
    private Integer state;
    @Builder.Default
    private Boolean deleted = false;
    @Builder.Default
    private Boolean isTop = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}