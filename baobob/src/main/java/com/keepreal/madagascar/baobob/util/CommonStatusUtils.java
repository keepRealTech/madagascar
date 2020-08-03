package com.keepreal.madagascar.baobob.util;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;

public class CommonStatusUtils {
    public static CommonStatus buildCommonStatus(ErrorCode errorCode) {
        return CommonStatus.newBuilder()
                .setRtn(errorCode.getNumber())
                .setMessage(errorCode.name())
                .build();
    }
}
