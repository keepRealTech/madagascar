package com.keepreal.madagascar.common.exceptions;

import com.keepreal.madagascar.common.CommonStatus;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents the generic business runtime exception.
 */
@Getter
public class KeepRealBusinessException extends RuntimeException {

    private ErrorCode errorCode;
    private String message;

    /**
     * Constructs a {@link KeepRealBusinessException} with generic throwable.
     *
     * @param throwable Throwable.
     */
    public KeepRealBusinessException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Constructs a {@link KeepRealBusinessException} with {@link ErrorCode}.
     *
     * @param errorCode {@link ErrorCode}.
     */
    public KeepRealBusinessException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }

    /**
     * Constructs a {@link KeepRealBusinessException} with {@link ErrorCode} and message.
     *
     * @param errorCode {@link ErrorCode}.
     * @param message   Customized error message.
     */
    public KeepRealBusinessException(ErrorCode errorCode, String message) {
        this(errorCode);
        this.message = message;
    }

    /**
     * Constructs a {@link KeepRealBusinessException} with {@link CommonStatus}.
     *
     * @param commonStatus {@link CommonStatus}.
     */
    public KeepRealBusinessException(CommonStatus commonStatus) {
        this(ErrorCode.forNumber(commonStatus.getRtn()), ExceptionMessageMap.get(commonStatus.getRtn()));
    }

    /**
     * Overrides the toString method.
     *
     * @return String.
     */
    @Override
    public String toString() {
        return "KeepRealBusinessException{" +
                "errorCode=" + errorCode +
                ", message='" + message + '\'' +
                '}';
    }

}
