package com.keepreal.madagascar.marty.service;

import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.keepreal.madagascar.common.PushPriority;
import com.keepreal.madagascar.fossa.FeedResponse;
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
    private final IslandService islandService;

    public PushNotificationService(JpushService jpushService,
                                   UmengPushService umengPushService,
                                   RedissonService redissonService,
                                   FeedService feedService,
                                   IslandService islandService) {
        this.jpushService = jpushService;
        this.umengPushService = umengPushService;
        this.redissonService = redissonService;
        this.feedService = feedService;
        this.islandService = islandService;
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


    /**
     * 向岛主(android)推送新问题通知
     *
     * @param userId    提问者 user id
     * @param feedId    动态(问题) feed id
     * @param tokenList 岛主android token list
     */
    public void umengPushAndroidNewQuestionNotification(String userId, String feedId, List<String> tokenList) {
        PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_QUESTION_VALUE);

        String title = pushPriorityInfo.getText();
        String text = feedService.retrieveFeedTextById(feedId, userId);

        JSONObject dataObject = new JSONObject();
        dataObject.put("feed_id", feedId);

        generatorCustom(pushPriorityInfo.getAndroidUrl(), dataObject);

        this.umengPushService.pushNotification(String.join(",", tokenList), title, text,
                generatorCustom(pushPriorityInfo.getAndroidUrl(), dataObject));
    }

    /**
     * 向岛主(ios)推送新问题通知
     *
     * @param userId    提问者 user id
     * @param feedId    动态(问题) feed id
     * @param tokenList 岛主 ios token list
     */
    public void jPushIosNewQuestionNotificationNotification(String userId, String feedId, List<String> tokenList) {
        PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_QUESTION_VALUE);

        String alert = pushPriorityInfo.getText();

        alert += "\n" + feedService.retrieveFeedTextById(feedId, userId);

        Map<String, String> extrasMap = new HashMap<>();
        extrasMap.put("URL", pushPriorityInfo.getIosUrl());

        this.jpushService.pushIosNotification(alert, extrasMap, tokenList.toArray(new String[0]));
    }

    /**
     * 向提问者(android)推送新回答通知
     *
     * @param userId    提问者 user id
     * @param feedId    动态(问题) feed id
     * @param tokenList 提问者 android token list
     */
    public void umengPushAndroidNewReplyNotification(String userId, String feedId, List<String> tokenList, Boolean isPublicVisible) {
        PushPriorityInfo pushPriorityInfo;
        String title;

        if (isPublicVisible) {
            pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_PUBLIC_REPLY_VALUE);
            String islandId = this.feedService.retrieveFeedInfoById(feedId, userId).getFeed().getIslandId();
            String hostId = this.islandService.retrieveIslandById(islandId).getIsland().getHostId();
            title = this.redissonService.getNickname(hostId) + pushPriorityInfo.getText();
        }else {
            pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_REPLY_VALUE);
            title = pushPriorityInfo.getText();
        }

        String text = feedService.retrieveFeedTextById(feedId, userId);

        JSONObject dataObject = new JSONObject();
        dataObject.put("feed_id", feedId);

        generatorCustom(pushPriorityInfo.getAndroidUrl(), dataObject);

        this.umengPushService.pushNotification(String.join(",", tokenList), title, text,
                generatorCustom(pushPriorityInfo.getAndroidUrl(), dataObject));
    }

    /**
     * 向提问者(ios)推送新回答通知
     *
     * @param userId    提问者 user id
     * @param feedId    动态(问题) feed id
     * @param tokenList 提问者 ios token list
     */
    public void jPushIosNewReplyNotificationNotification(String userId, String feedId, List<String> tokenList, Boolean isPublicVisible) {
        PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_REPLY_VALUE);

        String alert;

        if (isPublicVisible) {
            pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_PUBLIC_REPLY_VALUE);
            String islandId = this.feedService.retrieveFeedInfoById(feedId, userId).getFeed().getIslandId();
            String hostId = this.islandService.retrieveIslandById(islandId).getIsland().getHostId();
            alert = this.redissonService.getNickname(hostId) + pushPriorityInfo.getText();
        }else {
            pushPriorityInfo = PushPriorityConverter.convertTo(PushPriority.NEW_REPLY_VALUE);
            alert = pushPriorityInfo.getText();
        }

        alert += "\n" + feedService.retrieveFeedTextById(feedId, userId);

        Map<String, String> extrasMap = new HashMap<>();
        extrasMap.put("URL", pushPriorityInfo.getIosUrl());

        this.jpushService.pushIosNotification(alert, extrasMap, tokenList.toArray(new String[0]));
    }

    private JSONObject generatorCustom(String url, JSONObject dataObject) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", url);
        jsonObject.put("data", dataObject);
        return jsonObject;
    }

}
