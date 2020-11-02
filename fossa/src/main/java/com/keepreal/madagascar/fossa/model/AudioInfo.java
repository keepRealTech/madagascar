package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AudioInfo extends MediaInfo {
    private String url;
    private String title;
    private String thumbnailUrl;
    private String videoId;
    private Long duration;
}
