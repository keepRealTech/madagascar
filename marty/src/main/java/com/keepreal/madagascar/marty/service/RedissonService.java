package com.keepreal.madagascar.marty.service;

import com.keepreal.madagascar.common.PushPriority;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class RedissonService {

    private final RedissonClient redissonClient;
    private final UserService userService;

    public RedissonService(RedissonClient redissonClient,
                           UserService userService) {
        this.redissonClient = redissonClient;
        this.userService = userService;
    }

    public void putPushInfo(String userId, PushPriority pushPriority, String latestUserId) {
        RBucket<Object> pushType = redissonClient.getBucket("push:" + userId);
        RSet<Object> userSet = redissonClient.getSet("userSet:" + userId);
        if (!pushType.isExists()) {
            pushType.set(pushPriority.getNumber());
            pushType.expire(5L, TimeUnit.MINUTES);
            userSet.add(latestUserId);
            userSet.expire(5L, TimeUnit.MINUTES);
            return;
        }

        Integer redisPushType = (Integer) pushType.get();

        if (pushPriority.getNumber() == redisPushType) {
            userSet.add(latestUserId);
        }

        if (pushPriority.getNumber() > redisPushType) {
            pushType.set(pushPriority.getNumber());
            userSet.clear();
            userSet.add(latestUserId);
        }
    }

    public Map<String, List<String>> getToken(String pushKey) {
        String userId = pushKey.substring(pushKey.indexOf(':') + 1);
        Map<String, List<String>> resMap = new HashMap<>();

        RMap<Object, Object> tokenMap = redissonClient.getMap("token:" + userId);

        if (!tokenMap.isExists() || tokenMap.size() == 0) {
            tokenMap.expire(30L, TimeUnit.MINUTES);
            RetrieveDeviceTokenResponse response = userService.retrieveUserDeviceToken(userId);

            Object[] androidTokensList = response.getAndroidTokensList().toArray();
            Object[] iosTokensList = response.getIosTokensList().toArray();

            tokenMap.put("android", Arrays.asList(androidTokensList));
            tokenMap.put("ios", Arrays.asList(iosTokensList));
        }
        List<String> ios = (List<String>) tokenMap.get("ios");
        List<String> android = (List<String>) tokenMap.get("android");

        resMap.put("ios", ios);
        resMap.put("android", android);
        return resMap;
    }

    public String getNickname(String userId) {
        RBucket<Object> nickname = redissonClient.getBucket("nickname:" + userId);
        if (!nickname.isExists()) {
            UserMessage userMessage = userService.retrieveUserInfoById(userId);
            nickname.set(userMessage.getName());
            nickname.expire(30L, TimeUnit.MINUTES);
            return userMessage.getName();
        }
        return (String) nickname.get();
    }
}
