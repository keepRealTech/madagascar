package com.keepreal.madagascar.vanga.model;

import lombok.Getter;

/**
 * Represents the payment states.
 */
@Getter
public enum PaymentState {

    UNKNOWN(0),
    OPEN(1),
    CLOSED(2),
    ;

    private final int value;

    PaymentState(int value) {
        this.value = value;
    }

}