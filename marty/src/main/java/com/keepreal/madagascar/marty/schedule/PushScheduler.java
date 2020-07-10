package com.keepreal.madagascar.marty.schedule;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import com.keepreal.madagascar.marty.converter.PushPriorityConverter;
import com.keepreal.madagascar.marty.model.PushPriorityInfo;
import com.keepreal.madagascar.marty.service.JpushService;
import com.keepreal.madagascar.marty.service.UmengPushService;
import com.keepreal.madagascar.marty.service.UserService;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-07-09
 **/

@Service
public class PushScheduler {

    private final RedissonClient redissonClient;
    private final JpushService jpushService;
    private final UmengPushService umengPushService;
    private final UserService userService;

    public PushScheduler(RedissonClient redissonClient,
                         JpushService jpushService,
                         UmengPushService umengPushService,
                         UserService userService) {
        this.redissonClient = redissonClient;
        this.jpushService = jpushService;
        this.umengPushService = umengPushService;
        this.userService = userService;
    }

    public void push() {
        redissonClient.getKeys()
                .getKeysByPattern("push:*")
                .forEach(userId -> {
                    RMap<Object, Object> map = redissonClient.getMap(userId);
                    Integer type = (Integer) map.get("type");
                    Integer count = (Integer) map.get("count");
                    String name = (String) map.get("text");

                    Map<String, List<String>> token = getToken(userId);

                    String nickname = getNickname(name);

                    this.processAndJPushIosNotification(nickname, count, type, token.get("ios"));
                    this.processAndUmengPushNotification();
                });
    }

    private void processAndJPushIosNotification(String name, Integer count, Integer type, List<String> tokenList) {
        PushPriorityInfo pushPriorityInfo = PushPriorityConverter.convertTo(type);
        String notificationType = pushPriorityInfo.getName();
        String url = pushPriorityInfo.getIosUrl();
        String alert = name + "等" + count + "人给你" + notificationType;
        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("url", url);

        jpushService.pushIosNotification(alert, urlMap, (String[]) tokenList.toArray());
    }

    private void processAndUmengPushNotification() {

    }

    private Map<String, List<String>> getToken(String userId) {
        Map<String, List<String>> resMap = new HashMap<>();
        RMap<Object, Object> tokenMap = redissonClient.getMap("token:" + userId);

        if (tokenMap == null) {
            RetrieveDeviceTokenResponse response = userService.retrieveUserDeviceToken(userId);
            ProtocolStringList androidTokensList = response.getAndroidTokensList();
            ProtocolStringList iosTokensList = response.getIosTokensList();
            tokenMap.put("android", androidTokensList);
            tokenMap.put("ios", iosTokensList);
        }
        resMap.entrySet().addAll((Collection<? extends Map.Entry<String, List<String>>>) tokenMap.entrySet());
        return resMap;
    }

    private String getNickname(String userId) {
        RBucket<Object> nickname = redissonClient.getBucket("nickname:" + userId);
        if (!nickname.isExists()) {
            UserMessage userMessage = userService.retrieveUserInfoById(userId);
            nickname.set(userMessage.getName());
            return userMessage.getName();
        }
        return (String) nickname.get();
    }
}
