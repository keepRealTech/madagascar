package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HtmlInfo extends MediaInfo {
    private String content;
}
