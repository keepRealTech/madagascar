package com.keepreal.madagascar.fossa.model;

import lombok.Data;

@Data
public class VideoInfo extends MediaInfo {

    private String url;
    private String title;
    private String thumbnailUrl;
    private String videoId;
    private Long width;
    private Long height;
    private Long duration;

}
