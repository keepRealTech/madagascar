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
    PAYMEMBERSHIPH5(4),
    PAYSUPPORT(5),
    PAYSUPPORTH5(6),
    FEEDCHARGE(7),
    ;
    private final int value;

    WechatOrderType(int value) {
        this.value = value;
    }

    public static WechatOrderType fromValue(int value) {
        for (WechatOrderType type : values()) {
            if (value == type.getValue()) {
                return type;
            }
        }

        return WechatOrderType.UNKNOWN;
    }

}