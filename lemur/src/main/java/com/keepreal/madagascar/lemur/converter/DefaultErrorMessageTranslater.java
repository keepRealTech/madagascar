package com.keepreal.madagascar.lemur.converter;

import com.keepreal.madagascar.common.exceptions.ErrorCode;

/**
 * Represents a default implementation for error message translator.
 */
public class DefaultErrorMessageTranslater implements ErrorMessageTranslator {

    /**
     * Translates a {@link ErrorCode} into user readable message.
     *
     * @param errorCode Error code.
     * @return UX defined string.
     */
    @Override
    public String translate(ErrorCode errorCode) {
        switch (errorCode) {
            case REQUEST_GRPC_LOGIN_INVALID:
                return "登录失败，请重试";
            case REQUEST_ISLAND_NAME_EXISTED_ERROR:
                return "名称已被占用，请换一个";
            case REQUEST_ISLAND_SECRET_ERROR:
                return "暗号不对，终止交易，请重试";
            case REQUEST_GRPC_IMAGE_UPLOAD_ERROR:
                return "图片上传失败";
            case REQUEST_UNEXPECTED_ERROR:
                return "服务器开小差了，请稍后...";
            default:
                return errorCode.getValueDescriptor().getName();
        }
    }

}
