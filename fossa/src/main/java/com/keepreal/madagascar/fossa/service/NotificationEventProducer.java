package com.keepreal.madagascar.fossa.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.fossa.config.MqConfig;
import com.keepreal.madagascar.fossa.util.ProducerUtils;
import com.keepreal.madagascar.tenrecs.CommentEvent;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Represents the notification event producer.
 */
@Service
public class NotificationEventProducer {

    private ProducerBean producerBean;
    private MqConfig mqConfig;

    public NotificationEventProducer(ProducerBean producerBean,
                                     MqConfig mqConfig) {
        this.producerBean = producerBean;
        this.mqConfig = mqConfig;
    }

    public void produceNotificationEvent()  {

    }

    Message message = createMqMessage(commentMessage, feedMessage, feedMessage.getUserId());

    ProducerUtils.sendMessageAsync(producerBean, message);

    /**
     * create mq message
     *
     * @param commentMessage {@link CommentMessage}.
     * @param feedMessage    {@link FeedMessage}.
     * @param userId         receiver id.
     * @return  {@link Message}.
     */
    private Message createMqMessage(CommentMessage commentMessage, FeedMessage feedMessage, String userId) {
        CommentEvent commentEvent = CommentEvent.newBuilder()
                .setComment(commentMessage)
                .setFeed(feedMessage)
                .build();
        String uuid = UUID.randomUUID().toString();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_COMMENT)
                .setUserId(userId)
                .setCommentEvent(commentEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        return new Message(this.mqConfig.getTopic(), this.mqConfig.getTag(), uuid, event.toByteArray());
    }

}
