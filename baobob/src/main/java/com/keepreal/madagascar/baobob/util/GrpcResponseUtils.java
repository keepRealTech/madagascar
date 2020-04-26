package com.keepreal.madagascar.baobob.util;

import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import org.springframework.stereotype.Service;

/**
 * Represents a set of common status utility functions.
 */
@Service
public class GrpcResponseUtils {

    /**
     * Builds a common status by error code.
     *
     * @param errorCode {@link ErrorCode}.
     * @return {@link CommonStatus}.
     */
    public CommonStatus buildCommonStatus(ErrorCode errorCode) {
        return CommonStatus.newBuilder()
                .setRtn(errorCode.getNumber())
                .setMessage(errorCode.name())
                .build();
    }

    /**
     * Builds a login response with failed error code.
     *
     * @param errorCode {@link ErrorCode}.
     * @return {@link LoginResponse}.
     */
    public LoginResponse buildInvalidLoginResponse(ErrorCode errorCode) {
        return LoginResponse.newBuilder()
                .setStatus(this.buildCommonStatus(errorCode))
                .build();
    }

}
