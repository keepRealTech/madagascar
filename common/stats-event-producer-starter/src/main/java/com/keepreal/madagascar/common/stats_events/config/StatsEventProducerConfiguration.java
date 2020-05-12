package com.keepreal.madagascar.common.stats_events.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Represents the stats event producer configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "rocketmq.stats-event")
@Data
public class StatsEventProducerConfiguration {

    private String accessKey;
    private String secretKey;
    private String nameSrvAddr;
    private String topic;
    private String tag;

    /**
     * Sets the rmq properties.
     *
     * @return {@link Properties}.
     */
    private Properties getMqProperties() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        return properties;
    }

    /**
     * Constructs the stats event producer.
     *
     * @return {@link ProducerBean}.
     */
    @Bean(name = "stats-event-producer", initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean getStatsEventProducer() {
        ProducerBean producer = new ProducerBean();
        producer.setProperties(this.getMqProperties());
        return producer;
    }

}
