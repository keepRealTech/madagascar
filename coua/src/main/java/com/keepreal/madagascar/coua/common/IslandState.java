package com.keepreal.madagascar.coua.common;

import lombok.Getter;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Getter
public enum IslandState {

    LEAVE(0),
    HOST(1),
    ISLANDER(2),
    ;
    private int value;

    IslandState(int value) {
        this.value = value;
    }

}
