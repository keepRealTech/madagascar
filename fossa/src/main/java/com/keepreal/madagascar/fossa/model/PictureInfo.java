package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PictureInfo extends MediaInfo{
    private String url;
    private Long width;
    private Long height;
    private Long size;
}
