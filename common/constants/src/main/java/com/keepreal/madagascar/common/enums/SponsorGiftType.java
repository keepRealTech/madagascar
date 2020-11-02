package com.keepreal.madagascar.common.enums;

public enum SponsorGiftType {
    UNSORTED(0),
    DIET(1),
    ANIMAL(2),
    ITEM(3),
    SMILE_TOOLS(4),
    SPORTS_INSTRUMENT(5),
    SYMBOL(6);

    private final int value;
    SponsorGiftType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
