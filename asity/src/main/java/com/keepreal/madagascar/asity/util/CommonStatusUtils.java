package com.keepreal.madagascar.asity.util;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;

/**
 * Represents a set of common status utility functions.
 */
public class CommonStatusUtils {

    /**
     * Builds a common status by error code.
     * @param errorCode Error code {@link ErrorCode}.
     * @return Common status {@link CommonStatus}.
     */
    public static CommonStatus buildCommonStatus(ErrorCode errorCode) {
        return CommonStatus.newBuilder()
                .setRtn(errorCode.getNumber())
                .setMessage(errorCode.name())
                .build();
    }

}
