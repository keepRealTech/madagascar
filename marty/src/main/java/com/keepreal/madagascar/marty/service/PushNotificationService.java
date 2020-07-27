package com.keepreal.madagascar.marty.service;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.common.PushPriority;
import com.keepreal.madagascar.marty.converter.PushPriorityConverter;
import com.keepreal.madagascar.marty.model.PushPriorityInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {

    private final JpushService jpushService;
    private final UmengPushService umengPushService;
    private final RedissonService redissonService;
    private final FeedService feedService;

    public PushNotificationService(JpushService jpushService,
                                   UmengPushService umengPushService,
                                   RedissonService redissonService,
                                   FeedService feedService) {
        this.jpushService = jpushService;
        this.umengPushService = umengPushService;
        this.redissonService = redissonService;
        this.feedService = feedService;
    }

    public void jPushIosNotification(String alert, Integer type, List<String> tokenList) {
        if (tokenList != null && tokenList.size() > 0) {
            PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(type);

            alert += "\n" + pushPriorityInfo.getText();

            Map<String, String> extrasMap = new HashMap<>();
            extrasMap.put("URL", pushPriorityInfo.getIosUrl());

            jpushService.pushIosNotification(alert, extrasMap, tokenList.toArray(new String[0]));
        }
    }

    public void umengPushAndroidNotification(String title, Integer type, List<String> tokenList) {
        if (tokenList != null && tokenList.size() > 0) {

            PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(type);
            String text = pushPriorityInfo.getText();

            JSONObject dataObject = new JSONObject();
            dataObject.putAll(pushPriorityInfo.getExtrasMap());

            umengPushService.pushNotification(String.join(",", tokenList), title, text, generatorCustom(pushPriorityInfo.getAndroidUrl(), dataObject));
        }
    }

    public void jPushIosNewFeedNotification(String userId, String feedId, List<String> tokenList) {
        PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_FEED_VALUE);

        String alert = redissonService.getNickname(userId) + pushPriorityInfo.getText();

        alert += "\n" + feedService.retrieveFeedTextById(feedId, userId);

        Map<String, String> extrasMap = new HashMap<>();
        extrasMap.put("URL", pushPriorityInfo.getIosUrl() + feedId);

        jpushService.pushIosNotification(alert, extrasMap, tokenList.toArray(new String[0]));
    }

    public void umengPushAndroidNewFeedNotification(String userId, String feedId, List<String> tokenList) {
        PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_FEED_VALUE);

        String title = redissonService.getNickname(userId) + pushPriorityInfo.getText();

        String text = feedService.retrieveFeedTextById(feedId, userId);

        JSONObject dataObject = new JSONObject();
        dataObject.put("feed_id", feedId);

        generatorCustom(pushPriorityInfo.getAndroidUrl(), dataObject);

        umengPushService.pushNotification(String.join(",", tokenList), title, text, generatorCustom(pushPriorityInfo.getAndroidUrl(), dataObject));
    }

    private JSONObject generatorCustom(String url, JSONObject dataObject) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", url);
        jsonObject.put("data", dataObject);
        return jsonObject;
    }
}
