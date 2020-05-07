package com.keepreal.madagascar.fossa.util;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.NotificationType;
import com.keepreal.madagascar.tenrecs.CommentEvent;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-06
 **/

@Slf4j
public class ProducerUtils {

    public static void sendMessageAsync(Producer producer, Message message) {
        producer.sendAsync(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) { }
            @Override
            public void onException(OnExceptionContext context) {
                log.error("this messge send failure, message Id is {}", context.getMessageId());
            }
        });
    }
}
