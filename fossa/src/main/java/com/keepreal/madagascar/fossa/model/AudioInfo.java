package com.keepreal.madagascar.fossa.model;

import lombok.Data;

@Data
public class AudioInfo {
    private String url;
    private String thumbnailUrl;
    private String videoId;
    private Integer duration;
}
