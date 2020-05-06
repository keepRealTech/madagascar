package com.keepreal.madagascar.fossa.util;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-29
 **/

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
