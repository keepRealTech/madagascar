package com.keepreal.madagascar.marty.service;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import com.keepreal.madagascar.marty.model.PushType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class JpushService {

    private final JPushClient jPushClient;

    public JpushService(JPushClient jPushClient) {
        this.jPushClient = jPushClient;
    }

    public void pushIOSMessageByType(PushType pushType, String... registrationIds) {
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
        try {
            jPushClient.sendPush(PushPayload.newBuilder()
                    .setPlatform(Platform.ios())
                    .setAudience(Audience.registrationId(registrationIds))
                    .setMessage(Message.newBuilder()
                            .addExtra("type", pushType.getValue())
                            .addExtra("islandId", islandId)
                            .build())
                    .build());
        } catch (APIConnectionException | APIRequestException e) {
            e.printStackTrace();
        }
    }

    public void pushIosNotification(String alert, Map<String, String> extras, String... registrationIds) {
        try {
            jPushClient.sendIosNotificationWithRegistrationID(alert, extras, registrationIds);
        } catch (APIConnectionException | APIRequestException e) {
            e.printStackTrace();
        }
    }

    public void push(String title, String msgContent, String... registrationID) {
        PushPayload.Builder builder = PushPayload.newBuilder();
        builder.setPlatform(Platform.ios());
        builder.setAudience(Audience.registrationId(registrationID));
        builder.setMessage(Message.newBuilder()
                .setTitle(title)
                .setMsgContent(msgContent)
                .addExtra("type", PushType.PUSH_COMMENT.getValue())
                .build());
        PushPayload payload = builder.build();
        try {
            jPushClient.sendPush(payload);
        } catch (APIConnectionException | APIRequestException e) {
            e.printStackTrace();
        }
    }
}
