package com.keepreal.madagascar.tenrecs.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.google.protobuf.InvalidProtocolBufferException;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.mantella.FeedEventMessage;
import com.keepreal.madagascar.tenrecs.factory.FeedEventToNotificationFactory;
import com.keepreal.madagascar.tenrecs.model.Notification;
import com.keepreal.madagascar.tenrecs.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.keepreal.madagascar.mantella.FeedEventType.FEED_EVENT_CREATE;
import static com.keepreal.madagascar.mantella.FeedEventType.FEED_EVENT_UPDATE;

/**
 * Represents the logic consuming {@link FeedEventMessage}
 */
@Component
@Slf4j
public class FeedEventListener implements MessageListener {

    private final FeedEventToNotificationFactory feedEventToNotificationFactory;
    private final NotificationService notificationService;

    public FeedEventListener(FeedEventToNotificationFactory feedEventToNotificationFactory,
                             NotificationService notificationService) {
        this.feedEventToNotificationFactory = feedEventToNotificationFactory;
        this.notificationService = notificationService;
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
            FeedEventMessage feedEventMessage = FeedEventMessage.parseFrom(message.getBody());

            if (feedEventMessage.getType().equals(FEED_EVENT_CREATE)
                    && MediaType.MEDIA_QUESTION_VALUE == feedEventMessage.getFeedCreateEvent().getMediaTypeValue()) {
                Notification notification = this.feedEventToNotificationFactory.toNotification(feedEventMessage);
                this.notificationService.insert(notification);
                return Action.CommitMessage;
            }

            if (feedEventMessage.getType().equals(FEED_EVENT_UPDATE)) {
                Notification notification = this.feedEventToNotificationFactory.toNotification(feedEventMessage);
                this.notificationService.insert(notification);
                return Action.CommitMessage;
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
