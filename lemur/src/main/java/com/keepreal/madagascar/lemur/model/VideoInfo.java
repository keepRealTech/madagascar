package com.keepreal.madagascar.lemur.model;

import lombok.Data;

@Data
public class VideoInfo {

    private String playURL;
    private String title;
    private Long width;
    private Long height;
    private String duration;
    private String coverURL;
}
