package com.keepreal.madagascar.marty.service;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import com.keepreal.madagascar.marty.config.JPushConfig;
import com.keepreal.madagascar.marty.model.PushType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class JpushService {

    private final JPushClient jPushClient;

    public JpushService(JPushConfig jPushConfig) {
        this.jPushClient = new JPushClient(jPushConfig.getAppSecret(), jPushConfig.getAppKey());
    }

    public void pushIOSMessageByType(PushType pushType, String... registrationIds) {
        if (registrationIds.length == 0)
            return;
        try {
            jPushClient.sendPush(PushPayload.newBuilder()
                    .setPlatform(Platform.ios())
                    .setAudience(Audience.registrationId(registrationIds))
                    .setMessage(Message.newBuilder()
                            .setMsgContent("notification")
                            .addExtra("type", pushType.getValue())
                            .build())
                    .build());
        } catch (APIConnectionException | APIRequestException e) {
            e.printStackTrace();
        }
    }

    public void pushIOSNewFeedMessage(String islandId, PushType pushType, String... registrationIds) {
        if (registrationIds.length == 0)
            return;
        try {
            jPushClient.sendPush(PushPayload.newBuilder()
                    .setPlatform(Platform.ios())
                    .setAudience(Audience.registrationId(registrationIds))
                    .setMessage(Message.newBuilder()
                            .setMsgContent("notification")
                            .addExtra("type", pushType.getValue())
                            .addExtra("islandId", islandId)
                            .build())
                    .build());
        } catch (APIConnectionException | APIRequestException e) {
            log.error("error is {}", e.getMessage());
        }
    }

    public void pushIOSUpdateBulletinMessage(String chatGroupId, String bulletin, PushType pushType, String... registrationIds) {
        if (registrationIds.length == 0)
            return;
        try {
            jPushClient.sendPush(PushPayload.newBuilder()
                    .setPlatform(Platform.ios())
                    .setAudience(Audience.registrationId(registrationIds))
                    .setMessage(Message.newBuilder()
                            .setMsgContent("notification")
                            .addExtra("type", pushType.getValue())
                            .addExtra("chatGroupId", chatGroupId)
                            .addExtra("bulletin", bulletin)
                            .build())
                    .build());
        } catch (APIConnectionException | APIRequestException e) {
            e.printStackTrace();
        }
    }

    public void pushIosNotification(String alert, Map<String, String> extras, String... registrationIds) {
        if (registrationIds.length == 0)
            return;
        try {
            jPushClient.sendIosNotificationWithRegistrationID(alert, extras, registrationIds);
        } catch (APIConnectionException | APIRequestException e) {
            e.printStackTrace();
        }
    }
}
