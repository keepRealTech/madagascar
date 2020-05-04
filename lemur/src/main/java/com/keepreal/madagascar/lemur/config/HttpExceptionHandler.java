package com.keepreal.madagascar.lemur.config;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import swagger.model.DummyResponse;

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
     * @param request   Request.
     * @return {@link DummyResponse}.
     */
    @ExceptionHandler(value = {KeepRealBusinessException.class})
    protected ResponseEntity<DummyResponse> handleWebBussinessException(KeepRealBusinessException exception,
                                                                        WebRequest request) {
        if (ErrorCode.REQUEST_INVALID_ARGUMENT.equals(exception.getErrorCode())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, exception.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
