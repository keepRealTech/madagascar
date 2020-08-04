package com.keepreal.madagascar.baobob.loginExecutor.model;

import com.keepreal.madagascar.common.Gender;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Represents the wechat user info.
 */
@Builder
@Data
public class WechatUserInfo implements Serializable {

    private String name;
    private Gender gender;
    private String province;
    private String city;
    private String country;
    private String portraitImageUri;
    private String unionId;
    private String openId;

}
