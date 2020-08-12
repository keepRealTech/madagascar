package com.keepreal.madagascar.tenrecs.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the feed model in notification.
 */
@Builder
@Data
public class Feed {

    private String id;
    private String islandId;
    private String authorId;
    private String multiMediaType;
    private List<MediaInfo> mediaInfos;
    @Builder.Default
    private String text = "";
    @Builder.Default
    private List<String> imageUris = new ArrayList<>();
    private Long createdAt;

}
