package com.keepreal.madagascar.common.enums;

public enum SuperFollowState {
    ENABLED(1),
    SUSPEND(2),
    NONE(3);

    private final int value;
    SuperFollowState(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
