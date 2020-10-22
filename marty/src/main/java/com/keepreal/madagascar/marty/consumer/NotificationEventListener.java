package com.keepreal.madagascar.marty.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;

import com.keepreal.madagascar.common.PushPriority;
import com.keepreal.madagascar.marty.model.PushType;
import com.keepreal.madagascar.marty.service.PushService;
import com.keepreal.madagascar.marty.service.RedissonService;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the notification event listener.
 */
@Component
@Slf4j
public class NotificationEventListener implements MessageListener {

    private final PushService pushService;
    private final RedissonService redissonService;

    public NotificationEventListener(PushService pushService,
                                     RedissonService redissonService) {
        this.pushService = pushService;
        this.redissonService = redissonService;
    }

    /**
     * Implements the message consume method.
     *
     * @param message   {@link Message}.
     * @param context   {@link ConsumeContext}.
     * @return  {@link Action}.
     */
    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            if (Objects.isNull(message) || Objects.isNull(message.getBody())) {
                return Action.CommitMessage;
            }

            NotificationEvent notificationEvent = NotificationEvent.parseFrom(message.getBody());
            if (notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_COMMENT)) {
                String userId = notificationEvent.getUserId();
                pushService.pushMessageByType(userId, PushType.PUSH_COMMENT);
                if (!userId.equals(notificationEvent.getCommentEvent().getComment().getUserId())) {
                    this.redissonService.putPushInfo(userId, PushPriority.NEW_COMMENT, notificationEvent.getCommentEvent().getComment().getUserId());
                }
            }

            if (notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_REACTION)) {
                String userId = notificationEvent.getUserId();
                pushService.pushMessageByType(userId, PushType.PUSH_REACTION);
                if (!userId.equals(notificationEvent.getReactionEvent().getReaction().getUserId())) {
                    this.redissonService.putPushInfo(userId, PushPriority.NEW_LIKE, notificationEvent.getReactionEvent().getReaction().getUserId());
                }
            }

            if (notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_SUBSCRIBE)) {
                String userId = notificationEvent.getUserId();
                pushService.pushMessageByType(userId, PushType.PUSH_SUBSCRIBE);
                this.redissonService.putPushInfo(userId, PushPriority.NEW_SUBSCRIBE, notificationEvent.getSubscribeEvent().getSubscriberId());
            }

            if (notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_MEMBER) ||
                    notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_SUPPORT) ||
                    notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_FEED_PAYMENT)) {
                String userId = notificationEvent.getUserId();
                pushService.pushMessageByType(userId, PushType.PUSH_MEMBER);
                String latestUserId;
                if (notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_MEMBER)) {
                    latestUserId = notificationEvent.getMemberEvent().getMemberId();
                } else if (notificationEvent.getType().equals(NotificationEventType.NOTIFICATION_EVENT_NEW_SUPPORT)) {
                    latestUserId = notificationEvent.getSupportEvent().getUserId();
                } else {
                    latestUserId = notificationEvent.getFeedPaymentEvent().getUserId();
                }
                this.redissonService.putPushInfo(userId, PushPriority.NEW_MEMBERSHIP, latestUserId);
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
