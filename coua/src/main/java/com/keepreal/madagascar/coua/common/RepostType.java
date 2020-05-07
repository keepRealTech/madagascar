package com.keepreal.madagascar.coua.common;

import lombok.Getter;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-06
 **/

@Getter
public enum RepostType {

    ISLAND(0),
    FEED(1),
    ;

    private int code;

    RepostType(int code) {
        this.code = code;
    }
}
