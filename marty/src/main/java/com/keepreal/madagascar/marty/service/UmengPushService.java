package com.keepreal.madagascar.marty.service;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensResponse;
import com.keepreal.madagascar.marty.model.PushType;
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

    private final UmengConfiguration umengConfiguration;
    private final UmengPushClient umengPushClient;

    public UmengPushService(UmengConfiguration umengConfiguration,
                            UmengPushClient umengPushClient) {
        this.umengConfiguration = umengConfiguration;
        this.umengPushClient = umengPushClient;
    }

    public void pushMessageByType(String tokens, PushType pushType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", pushType.getValue());
        this.pushMessage(tokens, jsonObject);
    }

    public void pushNewFeedByType(String tokens, String islandId, PushType pushType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", pushType.getValue());
        jsonObject.put("islandId", islandId);
        this.pushMessage(tokens, jsonObject);
    }

    public void pushMessage(String tokens, JSONObject jsonObject) {
        // build android
        AndroidListCast androidListCast = new AndroidListCast(umengConfiguration.getAndroidAppKey());
        androidListCast.setDeviceToken(tokens);
        androidListCast.setDisplayType("message");
        androidListCast.setCustom(jsonObject);
        // push android
        umengPushClient.push(androidListCast.toString());
    }

    public void pushNotification(String tokens, String title, String text) {
        AndroidListCast androidListCast = new AndroidListCast(umengConfiguration.getAndroidAppKey());
        androidListCast.setDeviceToken(tokens);
        androidListCast.setTitle(title);
        androidListCast.setText(text);

        umengPushClient.push(androidListCast.toString());
    }

}
