package com.keepreal.madagascar.common.enums;

/**
 * banner type
 */
public enum BannerType {
    UNSORTED(0),
    PUBLIC(1),
    CREATOR(2),
    SUPER_FOLLOW(3);

    private final int value;
    BannerType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
