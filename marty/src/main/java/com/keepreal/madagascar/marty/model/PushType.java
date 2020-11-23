package com.keepreal.madagascar.marty.model;

import lombok.Getter;

@Getter
public enum PushType {

    PUSH_FEED(1001L),
    PUSH_COMMENT(1002L),
    PUSH_REACTION(1003L),
    PUSH_SUBSCRIBE(1004L),
    PUSH_MEMBER(1005L),
    PUSH_UPDATE_BULLETIN(1006L),
    PUSH_QUESTION(1007L),
    PUSH_REPLY(1008L),
    PUSH_FEED_WORKS(1009L),
    PUSH_FEED_TRANS_CODE_COMPLETE(1010L),
    ;

    private Long value;

    PushType(Long value) {
        this.value = value;
    }
}
