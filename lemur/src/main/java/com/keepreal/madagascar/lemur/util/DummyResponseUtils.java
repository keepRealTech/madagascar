package com.keepreal.madagascar.lemur.util;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.converter.DefaultErrorMessageTranslater;
import com.keepreal.madagascar.lemur.converter.ErrorMessageTranslator;
import swagger.model.DummyResponse;

/**
 * Represents a set of response utils.
 */
public class DummyResponseUtils {

    private final static ErrorMessageTranslator errorMessageTranslator = new DefaultErrorMessageTranslater();

    /**
     * Sets the return and message for a common response.
     *
     * @param response  Response.
     * @param errorCode Error code.
     */
    public static void setRtnAndMessage(DummyResponse response, ErrorCode errorCode) {
        response.setRtn(errorCode.getNumber());
        response.setMsg(DummyResponseUtils.errorMessageTranslator.translate(errorCode));
    }

    public static void setRtnAndMessage(DummyResponse response, CommonStatus commonStatus) {
        response.setRtn(commonStatus.getRtn());
        response.setMsg(commonStatus.getMessage());
    }

}
