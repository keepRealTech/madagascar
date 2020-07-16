package com.keepreal.madagascar.marty.model;

import lombok.Getter;

@Getter
public enum  PushType {

    PUSH_FEED(1001L),
    PUSH_COMMENT(1002L),
    PUSH_REACTION(1003L),
    PUSH_SUBSCRIBE(1004L),
    PUSH_MEMBER(1005L)
    ;

    private Long value;

    PushType(Long value) {
        this.value = value;
    }
}