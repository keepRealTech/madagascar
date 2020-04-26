package com.keepreal.madagascar.lemur.util;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import swagger.model.CommonResponse;

/**
 * Represents a set of response utils.
 */
public class ResponseUtils {

    /**
     * Sets the return and message for a common response.
     *
     * @param response  Response.
     * @param errorCode Error code.
     */

    public static void setRtnAndMessage(CommonResponse response, ErrorCode errorCode) {
        response.setRtn(errorCode.getNumber());
        response.setMsg(errorCode.getValueDescriptor().getName());
    }

    /**
     * Sets the return and message for a common response.
     *
     * @param response Response.
     * @param status   CommonStatus.
     */
    public static void setRtnAndMessage(CommonResponse response, CommonStatus status) {
        response.setRtn(status.getRtn());
        response.setMsg(status.getMessage());
    }

}
