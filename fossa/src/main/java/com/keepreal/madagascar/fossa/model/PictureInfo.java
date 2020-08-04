package com.keepreal.madagascar.fossa.model;

import lombok.Data;

@Data
public class PictureInfo extends MediaInfo{
    private String url;
    private Integer width;
    private Integer height;
    private Integer size;
}
