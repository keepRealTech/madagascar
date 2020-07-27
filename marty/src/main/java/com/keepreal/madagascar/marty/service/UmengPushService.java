package com.keepreal.madagascar.marty.service;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
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

        JSONObject dataObject = new JSONObject();
        dataObject.put("islandId", islandId);
        jsonObject.put("data", dataObject);
        this.pushMessage(tokens, jsonObject);
    }

    public void pushUpdateBulletin(String tokens, String chatGroupId, String bulletin, PushType pushType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", pushType.getValue());

        JSONObject dataObject = new JSONObject();
        dataObject.put("chatGroupId", chatGroupId);
        dataObject.put("bulletin", bulletin);
        jsonObject.put("data", dataObject);
        this.pushMessage(tokens, jsonObject);
    }

    private void pushMessage(String tokens, JSONObject jsonObject) {
        if (tokens.length() == 0)
            return;
        // build android
        AndroidListCast androidListCast = new AndroidListCast(umengConfiguration.getAndroidAppKey());
        androidListCast.setDeviceToken(tokens);
        androidListCast.setDisplayType("message");
        androidListCast.setCustom(jsonObject);
        // push android
        umengPushClient.push(androidListCast.toString());
    }

    public void pushNotification(String tokens, String title, String text, JSONObject jsonObject) {
        if (tokens.length() == 0)
            return;
        AndroidListCast androidListCast = new AndroidListCast(umengConfiguration.getAndroidAppKey());
        androidListCast.setCustom(jsonObject);
        androidListCast.setDeviceToken(tokens);
        androidListCast.setDisplayType("notification");
        androidListCast.setAfterOpen("go_custom");
        androidListCast.setTitle(title);
        androidListCast.setText(text);
        umengPushClient.push(androidListCast.toString());
    }

}
