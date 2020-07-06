package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.PushType;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-07-06
 **/

@Service
@Slf4j
public class RedissonService {

    private final RedissonClient redissonClient;

    public RedissonService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void putPushInfo(String userId, PushType pushType, String text) {
        RMap<Object, Object> redisPushMap = redissonClient.getMap(userId);
        if (redisPushMap == null) {
            redisPushMap.put("type", pushType.getNumber());
            redisPushMap.put("count", 1);
            redisPushMap.put("text", text);
            return;
        }

        Integer redisPushType = (Integer) redisPushMap.get("type");

        if (pushType.getNumber() == redisPushType) {
            redisPushMap.addAndGet("count", 1);
        }

        if (pushType.getNumber() > redisPushType) {
            redisPushMap.put("count", 1);
        }

        redisPushMap.put("text", text);
    }
}
