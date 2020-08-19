package com.keepreal.madagascar.marty.service;

import com.keepreal.madagascar.marty.model.PushType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MartyServiceTests {
    @Autowired
    PushService pushService;

    @Autowired
    RedissonService redissonService;

    @Test
    public void testPush01() {
        pushService.pushMessageByType("userId", PushType.PUSH_COMMENT);
    }

}
