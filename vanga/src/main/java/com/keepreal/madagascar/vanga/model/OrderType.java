package com.keepreal.madagascar.vanga.model;

import lombok.Getter;

/**
 * Represents the order type.
 */
@Getter
public enum OrderType {

    UNKNOWN(0),
    PAYSHELL(1),
    PAYMEMBERSHIP(2),
    PAYQUESTION(3),
    PAYMEMBERSHIPH5(4),
    PAYSUPPORT(5),
    PAYSUPPORTH5(6),
    PAYFEEDCHARGE(7),
    PAYFEEDCHARGEH5(8),
    ;
    private final int value;

    OrderType(int value) {
        this.value = value;
    }

    public static OrderType fromValue(int value) {
        for (OrderType type : values()) {
            if (value == type.getValue()) {
                return type;
            }
        }

        return OrderType.UNKNOWN;
    }

}