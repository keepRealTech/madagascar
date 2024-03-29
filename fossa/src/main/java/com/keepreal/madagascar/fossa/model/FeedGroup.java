package com.keepreal.madagascar.fossa.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EntityListeners;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

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
    private String name;
    private String description;
    @Builder.Default
    private Long lastFeedTime = 0L;
    @Builder.Default
    private String thumbnailUri = "";
    @Builder.Default
    private List<String> imageFeedIds = new ArrayList<>();
    @Builder.Default
    private NavigableSet<String> feedIds = new TreeSet<>();
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;

}