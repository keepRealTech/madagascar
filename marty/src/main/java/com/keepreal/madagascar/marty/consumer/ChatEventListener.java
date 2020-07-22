package com.keepreal.madagascar.marty.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.asity.ChatEvent;
import com.keepreal.madagascar.asity.ChatEventType;
import com.keepreal.madagascar.asity.UpdateBulletinEvent;
import com.keepreal.madagascar.marty.model.PushType;
import com.keepreal.madagascar.marty.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class ChatEventListener implements MessageListener {

    private final PushService pushService;

    public ChatEventListener(PushService pushService) {
        this.pushService = pushService;
    }

    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }

            ChatEvent chatEvent = ChatEvent.parseFrom(message.getBody());
            if (ChatEventType.CHAT_EVENT_UPDATE_BULLETIN.equals(chatEvent.getType())) {
                UpdateBulletinEvent event = chatEvent.getUpdateBulletinEvent();
                pushService.pushUpdateBulletinMessage(event.getChatGroupId(),
                        event.getUserId(),
                        event.getBulletin(),
                        PushType.PUSH_UPDATE_BULLETIN);
            }

            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted notification event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }
}
