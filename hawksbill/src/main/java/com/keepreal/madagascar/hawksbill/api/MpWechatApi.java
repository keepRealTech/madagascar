package com.keepreal.madagascar.hawksbill.api;

public class MpWechatApi {
    public static final String POST_TEMPLATE_MESSAGE = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
    public static final String POST_PERMANENT_QRCODE = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";
    public static final String GET_WECHAT_OFFICIAL_ACCOUNT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
}
