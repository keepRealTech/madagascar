package com.keepreal.madagascar.workflow.settler.model;

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
    PENDING(4),
    REFUNDING(5),
    REFUNDED(6),
    ;

    private final int value;

    PaymentState(int value) {
        this.value = value;
    }

}