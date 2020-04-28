package com.keepreal.madagascar.baobob.loginExecutor.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the wechat login info.
 */
@Builder
@Data
public class WechatLoginInfo {

    private String accessToken;
    private String openId;
    private String unionId;

}
