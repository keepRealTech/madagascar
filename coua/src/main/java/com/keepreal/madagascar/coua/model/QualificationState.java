package com.keepreal.madagascar.coua.model;

import lombok.Getter;

@Getter
public enum  QualificationState {
    UNKNOWN(0),
    REJECTED(1),
    PROCESSING(2),
    AUTHENTICATED(3),
    ;
    private final int value;

    QualificationState(int value) {
        this.value = value;
    }
}
