package com.keepreal.madagascar.vanga.model;

import lombok.Getter;

/**
 * Represents the payment type.
 */
@Getter
public enum PaymentType {

    UNKNOWN(0),
    WECHATPAY(1),
    SHELLPAY(2),
    WITHDRAW(3),
    ;
    private final int value;

    PaymentType(int value) {
        this.value = value;
    }

}
