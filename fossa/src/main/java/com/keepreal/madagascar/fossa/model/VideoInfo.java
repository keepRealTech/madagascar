package com.keepreal.madagascar.fossa.model;

import lombok.Data;

@Data
public class VideoInfo extends MediaInfo {
    private String url;
    private String thumbnailUrl;
    private String videoId;
    private Integer width;
    private Integer height;
    private Long duration;
}
