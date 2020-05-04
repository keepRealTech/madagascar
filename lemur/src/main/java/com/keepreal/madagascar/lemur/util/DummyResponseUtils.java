package com.keepreal.madagascar.lemur.util;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import swagger.model.CommonResponse;
import swagger.model.DummyResponse;

/**
 * Represents a set of response utils.
 */
public class DummyResponseUtils {

    /**
     * Sets the return and message for a common response.
     *
     * @param response  Response.
     * @param errorCode Error code.
     */
    public static void setRtnAndMessage(DummyResponse response, ErrorCode errorCode) {
        response.setRtn(errorCode.getNumber());
        response.setMsg(errorCode.getValueDescriptor().getName());
        response.setData("");
    }

}
