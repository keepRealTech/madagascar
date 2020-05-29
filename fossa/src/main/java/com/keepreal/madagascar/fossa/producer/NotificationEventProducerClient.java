package com.keepreal.madagascar.fossa.producer;

import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.fossa.config.NotificationEventProducerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-05
 **/

@Component
public class NotificationEventProducerClient {

    private final NotificationEventProducerConfiguration notificationEventProducerConfiguration;

    @Autowired
    public NotificationEventProducerClient(NotificationEventProducerConfiguration notificationEventProducerConfiguration) {
        this.notificationEventProducerConfiguration = notificationEventProducerConfiguration;
    }

    @Bean(name = "notification-event-producer", initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean buildProducer() {
        ProducerBean producer = new ProducerBean();
        producer.setProperties(notificationEventProducerConfiguration.getMqPropertie());
        return producer;
    }

}
