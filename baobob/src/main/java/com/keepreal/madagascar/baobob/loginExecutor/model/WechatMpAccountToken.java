package com.keepreal.madagascar.baobob.loginExecutor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the wechat media platform token.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WechatMpAccountToken {

    private String access_token;
    private Long expires_in;

}
