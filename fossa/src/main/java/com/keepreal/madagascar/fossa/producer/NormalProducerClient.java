package com.keepreal.madagascar.fossa.producer;

import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.fossa.config.MqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-05
 **/

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
        producer.setProperties(mqConfig.getMqPropertie());
        return producer;
    }
}
