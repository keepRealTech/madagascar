package com.keepreal.madagascar.marty.service;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import com.keepreal.madagascar.marty.MartyApplication;
import com.keepreal.madagascar.marty.model.PushType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = MartyApplication.class)
@RunWith(SpringRunner.class)
public class JpushServiceTest {

    @Autowired
    private JpushService jpushService;

    @Autowired
    private JPushClient jPushClient;

    public void testPush() {
        jpushService.push("test-title", "test-msgContent", "101d855909c9be491aa");
    }

    @Test
    public void test1() {
        try {
            PushResult pushResult = jPushClient.sendPush(PushPayload.newBuilder()
                    .setPlatform(Platform.ios())
                    .setAudience(Audience.registrationId("101d855909c9be491aa"))
                    .setMessage(Message.newBuilder()
                            .setMsgContent("test")
                            .addExtra("type", PushType.PUSH_FEED.getValue())
                            .addExtra("islandId", "123456789")
                            .build())
                    .build());

            System.out.println(pushResult);
        } catch (APIConnectionException | APIRequestException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() throws APIConnectionException, APIRequestException {
        String alert = "test-push2";
        Map<String, String> extras = new HashMap<>();
        extras.put("type", "123");
        extras.put("name", "测试用户1");
        extras.put("count", "20");
        String regId = "101d855909c9be491aa";
        jPushClient.sendIosNotificationWithRegistrationID(alert, extras, regId);
    }

    public void test2() throws APIConnectionException, APIRequestException {
        String alert = "";
        Map<String, String> extras = new HashMap<>();
        String regId = "";
        jPushClient.sendIosNotificationWithAlias(alert, extras, regId);
    }

    public void test3() {
        PushPayload pushPayload = PushPayload.newBuilder()

                .build();
    }
}
