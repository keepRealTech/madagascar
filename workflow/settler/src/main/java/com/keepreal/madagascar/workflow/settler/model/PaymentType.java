package com.keepreal.madagascar.workflow.settler.model;

import lombok.Getter;

/**
 * Represents the payment type.
 */
@Getter
public enum PaymentType {

    UNKNOWN(0),
    WECHATPAY(1),
    SHELLBUY(2),
    SHELLPAY(3),
    WITHDRAW(4),
    IOSBUY(5),
    SUPPORT(6),
    ALIPAY(7),
    ;
    private final int value;

    PaymentType(int value) {
        this.value = value;
    }

}
