package com.keepreal.madagascar.vanga.model;

import lombok.Getter;

/**
 * Represents the ios buy shells order states.
 */
@Getter
public enum IosOrderState {

    UNKNOWN(0),
    NOTPAY(1),
    USERPAYING(2),
    SUCCESS(3),
    CLOSED(4),
    REFUND(5),
    PAYERROR(6),
    REVOKED(7),
    REFUNDED(8),
    SANDBOXPAYERROR(9),
    ;

    private final int value;

    IosOrderState(int value) {
        this.value = value;
    }

}
