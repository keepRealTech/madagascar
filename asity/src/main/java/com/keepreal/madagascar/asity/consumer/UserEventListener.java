package com.keepreal.madagascar.asity.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.asity.service.RongCloudService;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.coua.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the user event listener.
 */
@Component
@Slf4j
public class UserEventListener implements MessageListener {

    private final RongCloudService rongCloudService;

    /**
     * Constructs the user event listener.
     *
     * @param rongCloudService {@link RongCloudService}.
     */
    public UserEventListener(RongCloudService rongCloudService) {
        this.rongCloudService = rongCloudService;
    }

    /**
     * Consumes the user event.
     *
     * @param message {@link Message}.
     * @param context {@link ConsumeContext}.
     * @return {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }
            UserEvent userEvent = UserEvent.parseFrom(message.getBody());

            switch (userEvent.getType()) {
                case USER_EVENT_CREATE:
                    this.rongCloudService.sendMessage(new String[]{userEvent.getCreateUserEvent().getUserId()}, Templates.ASITY_CREATE_USER_CONTENT_DEFAULT);
                    this.rongCloudService.sendMessage(new String[]{userEvent.getCreateUserEvent().getUserId()}, Templates.ASITY_CREATE_USER_CONTENT_POLL);
                    return Action.CommitMessage;
                case USER_EVENT_NONE:
                case UNRECOGNIZED:
                default:
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
