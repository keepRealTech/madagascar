package com.keepreal.madagascar.marty.service;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensResponse;
import com.keepreal.madagascar.marty.umengPush.UmengPushClient;
import com.keepreal.madagascar.marty.umengPush.android.AndroidListCast;
import com.keepreal.madagascar.marty.config.UmengConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Represents the umeng push service.
 */
@Service
@Slf4j
public class UmengPushService {

    private final IslandService islandService;
    private final UmengConfiguration umengConfiguration;
    private final UmengPushClient umengPushClient;
    private final UserService userService;

    public UmengPushService(IslandService islandService,
                            UmengConfiguration umengConfiguration,
                            UmengPushClient umengPushClient,
                            UserService userService) {
        this.islandService = islandService;
        this.umengConfiguration = umengConfiguration;
        this.umengPushClient = umengPushClient;
        this.userService = userService;
    }

    public void pushFeed(String islandId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", 1001L);
        jsonObject.put("islandId", islandId);
        pushIslanders(islandId, jsonObject);
    }

    public void pushComment(String userId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", 1002L);
        jsonObject.put("islandId", "");
        pushUser(userId, jsonObject);
    }

    public void pushReaction(String userId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", 1003L);
        jsonObject.put("islandId", "");
        pushUser(userId, jsonObject);
    }

    public void pushSubscribe(String userId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", 1004L);
        jsonObject.put("islandId", "");
        pushUser(userId, jsonObject);
    }

    public void pushMember(String userId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "member");
        jsonObject.put("islandId", "");
        pushUser(userId, jsonObject);
    }

    public void push(String tokens, JSONObject jsonObject) {
        // build android
        AndroidListCast androidListCast = new AndroidListCast(umengConfiguration.getAndroidAppKey());
        androidListCast.setDeviceToken(tokens);
        androidListCast.setDisplayType("message");
        androidListCast.setCustom(jsonObject);
        // push android
        umengPushClient.push(androidListCast.toString());

        // build ios
        // push ios
    }

    public void pushUser(String userId, JSONObject jsonObject) {
        RetrieveDeviceTokenResponse response = userService.retrieveUserDeviceToken(userId);
        String androidTokenStr = response.getAndroidTokensList().toString();
        push(androidTokenStr.substring(1, androidTokenStr.length() - 1), jsonObject);
    }

    public void pushIslanders(String islandId, JSONObject jsonObject) {
        int page = 0;
        int pageSize = 500;
        RetrieveDeviceTokensResponse response;
        do {
            PageRequest pageRequest = PageRequest.newBuilder()
                    .setPage(page++)
                    .setPageSize(pageSize)
                    .build();
            response = islandService.getDeviceTokenList(islandId, pageRequest);
            String tokenStr = response.getAndroidTokensList().toString();
            push(tokenStr.substring(1, tokenStr.length() - 1), jsonObject);
        } while (response.getPageResponse().getHasMore());
    }
}
