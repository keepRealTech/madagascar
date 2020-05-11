package com.keepreal.madagascar.lemur.converter;

import com.keepreal.madagascar.common.exceptions.ErrorCode;

/**
 * Represents the error message translator interface.
 */
public interface ErrorMessageTranslator {

    String translate(ErrorCode errorCode);

}
