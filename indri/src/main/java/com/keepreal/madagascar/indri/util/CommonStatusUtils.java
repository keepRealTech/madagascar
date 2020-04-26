package com.keepreal.madagascar.indri.util;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * Represents a set of common status utility functions.
 */
@Service
public class CommonStatusUtils {

    /**
     * Builds a common status by error code.
     * @param errorCode Error code {@link ErrorCode}.
     * @return Common status {@link CommonStatus}.
     */
    public CommonStatus buildCommonStatus(ErrorCode errorCode) {
        return CommonStatus.newBuilder()
                .setRtn(errorCode.getNumber())
                .setMessage(errorCode.name())
                .build();
    }

}
