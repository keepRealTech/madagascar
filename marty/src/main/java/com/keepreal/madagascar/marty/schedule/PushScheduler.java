package com.keepreal.madagascar.marty.schedule;

import com.keepreal.madagascar.marty.service.PushNotificationService;
import com.keepreal.madagascar.marty.service.RedissonService;
import com.keepreal.madagascar.marty.util.AutoRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-07-09
 **/

@Service
@Slf4j
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
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, "push-schedule")) {
            redissonClient.getKeys()
                    .getKeysByPattern("push:*")
                    .forEach(pushKey -> {
                        RBucket<Object> pushType = redissonClient.getBucket(pushKey);
                        Integer type = (Integer) pushType.get();
                        RSet<Object> userSet = redissonClient.getSet("userSet:" + pushKey.split(":")[1]);
                        Integer count = userSet.size();
                        String latestUserId = (String) userSet.random();

                        pushType.delete();
                        userSet.delete();
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
