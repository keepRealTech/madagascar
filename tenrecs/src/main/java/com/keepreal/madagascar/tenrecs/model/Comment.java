package com.keepreal.madagascar.tenrecs.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the comment model in notification.
 */
@Builder
@Data
public class Comment {

    private String id;
    @Builder.Default
    private String content = "";
    private String feedId;
    private String authorId;
    private String replyToId;
    private Long createdAt;

}