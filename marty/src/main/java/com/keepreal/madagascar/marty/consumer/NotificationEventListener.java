package com.keepreal.madagascar.marty.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.marty.service.UmengPushService;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class NotificationEventListener implements MessageListener {

    private final UmengPushService umengPushService;

    public NotificationEventListener(UmengPushService umengPushService) {
        this.umengPushService = umengPushService;
    }

    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }

            NotificationEvent notificationEvent = NotificationEvent.parseFrom(message.getBody());
            if (notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_COMMENT)) {
                String islandId = notificationEvent.getCommentEvent().getFeed().getIslandId();
                umengPushService.pushComment(islandId);
            }

            if (notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_REACTION)) {
                String islandId = notificationEvent.getReactionEvent().getFeed().getIslandId();
                umengPushService.pushReaction(islandId);
            }
            return Action.CommitMessage;
        } catch (DuplicateKeyException exception) {
            log.warn("Duplicated consumption, skipped.");
            return Action.CommitMessage;
        } catch (InvalidProtocolBufferException e) {
            log.warn("Bad formatted notification event, skipped.");
            return Action.CommitMessage;
        } catch (Exception e) {
            return Action.ReconsumeLater;
        }
    }
}
