package com.keepreal.madagascar.vanga.model;

import lombok.Getter;

/**
 * Represents the wechat order type.
 */
@Getter
public enum WechatOrderType {

    UNKNOWN(0),
    PAYSHELL(1),
    PAYMEMBERSHIP(2),
    PAYQUESTION(3),
    ;
    private final int value;

    WechatOrderType(int value) {
        this.value = value;
    }

}