package com.keepreal.madagascar.fossa.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Represents the feed event producer configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "rocketmq.feed")
@Data
public class FeedEventProducerConfiguration {

    private String accessKey;
    private String secretKey;
    private String nameSrvAddr;
    private String topic;
    private String tag;

    public Properties getMqProperties() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        return properties;
    }

    /**
     * Builds the order producer for feed events.
     *
     * @return {@link OrderProducerBean}.
     */
    @Bean(name = "feed-event-producer", initMethod = "start", destroyMethod = "shutdown")
    public OrderProducerBean buildProducer() {
        OrderProducerBean orderProducer = new OrderProducerBean();
        orderProducer.setProperties(this.getMqProperties());
        return orderProducer;
    }

}
