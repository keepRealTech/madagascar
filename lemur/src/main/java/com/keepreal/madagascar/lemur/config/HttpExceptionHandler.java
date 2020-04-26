package com.keepreal.madagascar.lemur.config;

import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import swagger.model.CommonResponse;

/**
 * Represents a controller advice entity that handles exceptions.
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class HttpExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles the {@link KeepRealBusinessException}.
     *
     * @param exception {@link KeepRealBusinessException}.
     * @param request Request.
     * @return {@link CommonResponse}.
     */
    @ExceptionHandler(value = {KeepRealBusinessException.class})
    protected ResponseEntity<CommonResponse> handleWebBussinessException(KeepRealBusinessException exception,
                                                                         WebRequest request) {
        CommonResponse response = new CommonResponse();
        ResponseUtils.setRtnAndMessage(response, exception.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
