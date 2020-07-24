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
            case REQUEST_NAME_INVALID:
                return "名称不符合规范，请换一个";
            case REQUEST_ISLAND_SECRET_ERROR:
                return "暗号不对，终止交易，请重试";
            case REQUEST_GRPC_IMAGE_UPLOAD_ERROR:
                return "图片上传失败";
            case REQUEST_UNEXPECTED_ERROR:
                return "服务器开小差了，请稍后...";
            case REQUEST_IMAGE_NUMBER_TOO_LARGE:
                return "单动态图片数量过大";
            case REQUEST_ISLAND_NOT_FOUND_ERROR:
                return "你已不在岛内，请先登岛";
            case REQUEST_GRPC_LOGIN_FROZEN:
                return "登陆失败，你的账号被暂时锁定";
            case REQUEST_FEED_NOT_FOUND_ERROR:
                return "该动态已被删除";
            case REQUEST_COMMENT_NOT_FOUND_ERROR:
                return "该评论已被删除";
            case REQUEST_GRPC_TOKEN_EXPIRED:
                return "哎呀，重新登录试试";
            case REQUEST_USER_BALANCE_WITHDRAW_DAY_LIMIT_ERROR:
                return "单日提现上限为两万人民币，请重新输入";
            case REQUEST_MEMBERSHIP_DELETE_ERROR:
                return "已经有用户订阅的会员不能删除哦";
            case REQUEST_USER_SHELL_INSUFFICIENT_ERROR:
                return "当前贝壳余额不足，请充值后再试";
            case REQUEST_CHATGROUP_MAX_MEMBER_REACHED_ERROR:
                return "群内人数已达上限，太拥挤啦，去别的群看看吧";
            case REQUEST_LOW_VERSION_ERROR:
                return "岛民你好，我们发布了重大更新，请去App Store下载最新版本";
            case REQUEST_CHATGROUP_NOT_FOUND_ERROR:
                return "该群已被群主解散了";
            case REQUEST_ISLAND_CHATGROUP_LIMIT_ERROR:
                return "岛内群聊数已经达到上限";
            default:
                return errorCode.getValueDescriptor().getName();
        }
    }

}
