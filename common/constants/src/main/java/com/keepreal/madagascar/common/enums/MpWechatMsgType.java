package com.keepreal.madagascar.common.enums;

/**
 * mp wechat msg type
 */
public enum MpWechatMsgType {
    TEXT("text");


    private final String value;
    MpWechatMsgType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
