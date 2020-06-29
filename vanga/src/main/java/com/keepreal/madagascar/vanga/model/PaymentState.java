package com.keepreal.madagascar.vanga.model;

import lombok.Getter;

/**
 * Represents the payment states.
 */
@Getter
public enum PaymentState {

    UNKNOWN(0),
    DRAFTED(1),
    OPEN(2),
    CLOSED(3),
    ;

    private final int value;

    PaymentState(int value) {
        this.value = value;
    }

}