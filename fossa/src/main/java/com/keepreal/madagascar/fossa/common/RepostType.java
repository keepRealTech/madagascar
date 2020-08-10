package com.keepreal.madagascar.fossa.common;

import lombok.Getter;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-07
 **/

@Getter
public enum RepostType {

    ISLAND(0),
    FEED(1),
    ;

    private int value;

    RepostType(int value) {
        this.value = value;
    }
}
