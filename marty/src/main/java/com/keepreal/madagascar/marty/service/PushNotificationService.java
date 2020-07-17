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

            jpushService.pushIosNotification(alert, extrasMap, (String[]) tokenList.toArray());
        }
    }

    public void umengPushAndroidNotification(String title, Integer type, List<String> tokenList) {
        if (tokenList != null && tokenList.size() > 0) {

            PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(type);
            String text = pushPriorityInfo.getText();

            String tokenString = tokenList.toString();

            Map<String, String> extrasMap = pushPriorityInfo.getExtrasMap();
            extrasMap.put("URL", pushPriorityInfo.getAndroidUrl());
            JSONObject jsonObject = new JSONObject();
            jsonObject.putAll(extrasMap);
            umengPushService.pushNotification(tokenString.substring(1, tokenString.length() - 1), title, text, jsonObject);
        }
    }

    public void jPushIosNewFeedNotification(String userId, String feedId, List<String> tokenList) {
        PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_FEED_VALUE);

        String alert = redissonService.getNickname(userId) + pushPriorityInfo.getText();

        alert += "\n" + feedService.retrieveFeedTextById(feedId, userId);

        Map<String, String> extrasMap = new HashMap<>();
        extrasMap.put("URL", pushPriorityInfo.getIosUrl() + feedId);

        jpushService.pushIosNotification(alert, extrasMap, (String[]) tokenList.toArray());
    }

    public void umengPushAndroidNewFeedNotification(String userId, String feedId, List<String> tokenList) {
        PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_FEED_VALUE);

        String title = redissonService.getNickname(userId) + pushPriorityInfo.getText();

        String text = feedService.retrieveFeedTextById(feedId, userId);

        String tokenString = tokenList.toString();

        Map<String, String> extrasMap = pushPriorityInfo.getExtrasMap();
        extrasMap.put("feed_id", feedId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(extrasMap);
        umengPushService.pushNotification(tokenString.substring(1, tokenString.length() - 1), title, text, jsonObject);
    }
}
