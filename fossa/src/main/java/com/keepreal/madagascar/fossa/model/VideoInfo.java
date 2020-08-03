package com.keepreal.madagascar.fossa.model;

import lombok.Data;

@Data
public class VideoInfo {
    private String url;
    private String thumbnailUrl;
    private String videoId;
    private Integer width;
    private Integer height;
    private Integer duration;
}
