package com.keepreal.madagascar.marty.service;

import com.keepreal.madagascar.common.PushPriority;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;


@Service
public class RedissonService {

    private final RedissonClient redissonClient;

    public RedissonService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void putPushInfo(String userId, PushPriority pushPriority, String text) {
        RMap<Object, Object> redisPushMap = redissonClient.getMap(userId);
        if (redisPushMap == null || redisPushMap.size() == 0) {
            redisPushMap.put("type", pushPriority.getNumber());
            redisPushMap.put("count", 1);
            redisPushMap.put("text", text);
            return;
        }

        Integer redisPushType = (Integer) redisPushMap.get("type");

        if (pushPriority.getNumber() == redisPushType) {
            redisPushMap.addAndGet("count", 1);
        }

        if (pushPriority.getNumber() > redisPushType) {
            redisPushMap.put("type", pushPriority.getNumber());
            redisPushMap.put("count", 1);
        }

        redisPushMap.put("text", text);
    }
}
