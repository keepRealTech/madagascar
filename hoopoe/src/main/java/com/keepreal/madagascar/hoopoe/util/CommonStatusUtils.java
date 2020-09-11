package com.keepreal.madagascar.hoopoe.util;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;

/**
 *  build {@link CommonStatus}
 */
public class CommonStatusUtils {

    public static CommonStatus buildCommonStatus(ErrorCode errorCode) {
        return CommonStatus.newBuilder()
                .setRtn(errorCode.getNumber())
                .setMessage(errorCode.name())
                .build();
    }

    public static CommonStatus getSuccStatus() {
        return buildCommonStatus(ErrorCode.REQUEST_SUCC);
    }

}
