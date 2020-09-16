package com.keepreal.madagascar.vanga.model;

import lombok.Getter;

/**
 * Represents the wechat order states.
 */
@Getter
public enum OrderState {

    UNKNOWN(0),
    NOTPAY(1),
    USERPAYING(2),
    SUCCESS(3),
    CLOSED(4),
    REFUNDING(5),
    PAYERROR(6),
    REVOKED(7),
    REFUNDED(8),
    ;

    private final int value;

    OrderState(int value) {
        this.value = value;
    }

}