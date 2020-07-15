package com.keepreal.madagascar.marty.schedule;

import com.keepreal.madagascar.marty.service.PushNotificationService;
import com.keepreal.madagascar.marty.service.RedissonService;
import com.keepreal.madagascar.marty.util.AutoRedisLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-07-09
 **/

@Service
public class PushScheduler {

    private final RedissonClient redissonClient;
    private final RedissonService redissonService;
    private final PushNotificationService pushNotificationService;

    public PushScheduler(RedissonClient redissonClient,
                         RedissonService redissonService,
                         PushNotificationService pushNotificationService) {
        this.redissonClient = redissonClient;
        this.redissonService = redissonService;
        this.pushNotificationService = pushNotificationService;
    }

    @Scheduled(cron = "0 0/5 * * * *")
    public void push() {
       try(AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, "push-schedule")) {
            redissonClient.getKeys()
                    .getKeysByPattern("push:*")
                    .forEach(pushKey -> {
                        RMap<Object, Object> map = redissonClient.getMap(pushKey);
                        Integer type = (Integer) map.get("type");
                        Integer count = (Integer) map.get("count");
                        String latestUserId = (String) map.get("latestUserId");

                        Map<String, List<String>> token = redissonService.getToken(pushKey);

                        String nickname = redissonService.getNickname(latestUserId);

                        this.pushNotificationService.jPushIosNotification(getTitle(nickname, count), type, token.get("ios"));
                        this.pushNotificationService.umengPushAndroidNotification(getTitle(nickname, count), type, token.get("android"));
                    });
        }
    }

    private String getTitle(String name, Integer count) {
        String title = name;
        if (count > 1) {
            title += "等" + count + "人";
        }
        return title;
    }
}
