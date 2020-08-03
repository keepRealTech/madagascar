package com.keepreal.madagascar.baobob.loginExecutor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WechatOffiAccountToken {
    private String access_token;
    private Long expires_in;
}
