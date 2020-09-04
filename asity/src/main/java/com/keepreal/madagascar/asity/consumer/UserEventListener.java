package com.keepreal.madagascar.asity.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.asity.service.RongCloudService;
import com.keepreal.madagascar.coua.UserEvent;
import com.keepreal.madagascar.coua.UserEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class UserEventListener implements MessageListener {

    private final RongCloudService rongCloudService;

    public UserEventListener(RongCloudService rongCloudService) {
        this.rongCloudService = rongCloudService;
    }

    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }
            UserEvent userEvent = UserEvent.parseFrom(message.getBody());

            if (UserEventType.USER_EVENT_CREATE.equals(userEvent.getType())) {
                this.rongCloudService.sentCreateUserNotice(userEvent.getCreateUserEvent());
                return Action.CommitMessage;
            }

            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted user event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }
}
