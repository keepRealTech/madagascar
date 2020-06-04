package com.keepreal.madagascar.marty.service;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.marty.umengPush.UmengPushClient;
import com.keepreal.madagascar.marty.umengPush.android.AndroidListCast;
import com.keepreal.madagascar.marty.config.UmengConfiguration;
import org.springframework.stereotype.Service;

/**
 * Represents the umeng push service.
 */
@Service
public class UmengPushService {

    private final IslandService islandService;
    private final UmengConfiguration umengConfiguration;
    private final UmengPushClient umengPushClient;

    public UmengPushService(IslandService islandService,
                            UmengConfiguration umengConfiguration,
                            UmengPushClient umengPushClient) {
        this.islandService = islandService;
        this.umengConfiguration = umengConfiguration;
        this.umengPushClient = umengPushClient;
    }

    public void pushFeed(String islandId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "feed");
        jsonObject.put("islandId", islandId);
        push(islandId, jsonObject);
    }

    public void pushComment(String islandId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "comment");
        jsonObject.put("islandId", islandId);
        push(islandId, jsonObject);
    }

    public void pushReaction(String islandId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "reaction");
        jsonObject.put("islandId", islandId);
        push(islandId, jsonObject);
    }

    public void push(String islandId, JSONObject jsonObject) {
        String deviceTokenListString = islandService.getDeviceTokenList(islandId).toString();
        String tokens = deviceTokenListString.substring(1, deviceTokenListString.length() - 1);
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
}
