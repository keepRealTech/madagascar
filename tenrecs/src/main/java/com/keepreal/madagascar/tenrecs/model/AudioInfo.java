package com.keepreal.madagascar.tenrecs.model;

import lombok.Data;

@Data
public class AudioInfo extends MediaInfo {
    private String url;
    private String title;
    private String thumbnailUrl;
    private String videoId;
    private Long duration;
}
