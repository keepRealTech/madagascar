package com.keepreal.madagascar.fossa.util;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import lombok.extern.slf4j.Slf4j;

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
