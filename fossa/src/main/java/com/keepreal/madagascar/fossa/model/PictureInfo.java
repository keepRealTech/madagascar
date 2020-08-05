package com.keepreal.madagascar.fossa.model;

import lombok.Data;

@Data
public class PictureInfo extends MediaInfo{
    private String url;
    private Long width;
    private Long height;
    private Long size;
}
