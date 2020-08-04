package com.keepreal.madagascar.baobob.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Objects;

public class SingletonTokenUtils {

    private static final String GET_WECHAT_OFFICIAL_ACCOUNT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private String mpWechatAccessToken;
    private Long expiredTimestamp;

    private SingletonTokenUtils() {}

    private static SingletonTokenUtils singleton = null;

    public static SingletonTokenUtils getInstance() {
        if (Objects.isNull(singleton)) {
            singleton = new SingletonTokenUtils();
        }
        return singleton;
    }

    public String getAccessToken(String appId, String appSecret) {
        SingletonTokenUtils singletonTokenUtils = SingletonTokenUtils.getInstance();
        String mpWechatAccessToken = singletonTokenUtils.mpWechatAccessToken;
        Long expiredTimestamp = singletonTokenUtils.expiredTimestamp;
        Long currentTimeMillis = System.currentTimeMillis();

        if (Objects.nonNull(mpWechatAccessToken) && Objects.nonNull(expiredTimestamp) && currentTimeMillis <= expiredTimestamp) {
            return mpWechatAccessToken;
        }

        String getTokenUrl = String.format(GET_WECHAT_OFFICIAL_ACCOUNT_ACCESS_TOKEN_URL +
                        "?grant_type=client_credential&appid=%s&secret=%s",
                appId,
                appSecret);
        JSONObject response = WebClient.create(getTokenUrl)
                .get()
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();
        String access_token = String.valueOf(response.get("access_token"));
        String expires_in = String.valueOf(response.get("expires_in"));
        singletonTokenUtils.mpWechatAccessToken = access_token;
        singletonTokenUtils.expiredTimestamp = System.currentTimeMillis() + (Long.parseLong(expires_in) - 200) * 1000L;
        return access_token;
    }
}
