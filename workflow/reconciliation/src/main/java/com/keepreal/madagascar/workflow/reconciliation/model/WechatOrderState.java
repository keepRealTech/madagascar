package com.keepreal.madagascar.workflow.reconciliation.model;

import lombok.Getter;

/**
 * Represents the wechat order states.
 */
@Getter
public enum WechatOrderState {

    UNKNOWN(0),
    NOTPAY(1),
    USERPAYING(2),
    SUCCESS(3),
    CLOSED(4),
    REFUND(5),
    PAYERROR(6),
    REVOKED(7),
    REFUNDED(8),
    ;

    private final int value;

    WechatOrderState(int value) {
        this.value = value;
    }

}