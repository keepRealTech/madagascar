package com.keepreal.madagascar.coua.producer;

import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.coua.config.MqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NormalProducerClient {

    private final MqConfig mqConfig;

    @Autowired
    public NormalProducerClient(MqConfig mqConfig) {
        this.mqConfig = mqConfig;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean buildProducer() {
        ProducerBean producer = new ProducerBean();
        producer.setProperties(mqConfig.getMqProperties());
        return producer;
    }
}
