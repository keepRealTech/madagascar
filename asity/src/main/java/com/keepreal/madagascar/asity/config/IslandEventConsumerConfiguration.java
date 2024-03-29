package com.keepreal.madagascar.asity.config;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.keepreal.madagascar.asity.consumer.IslandEventListener;
import com.keepreal.madagascar.asity.consumer.NotificationEventListener;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represents the Rocket mq configurations.
 */
@Configuration
@ConfigurationProperties(prefix = "rocketmq.island", ignoreUnknownFields = false)
@Data
public class IslandEventConsumerConfiguration {
    private final IslandEventListener islandEventListener;
    private String accessKey;
    private String secretKey;
    private String nameSrvAddr;
    private String topic;
    private String groupId;
    private String tag;

    /**
     * Constructs the consumer bean {@link ConsumerBean}.
     *
     * @return {@link ConsumerBean}.
     */
    @Bean(name = "island-event-consumer", initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean buildConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();

        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        properties.setProperty(PropertyKeyConst.GROUP_ID, this.getGroupId());
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);

        Map<Subscription, MessageListener> subscriptionTable = new HashMap<>();
        Subscription subscription = new Subscription();
        subscription.setTopic(this.getTopic());
        subscription.setExpression(this.getTag());
        subscriptionTable.put(subscription, this.islandEventListener);
        consumerBean.setSubscriptionTable(subscriptionTable);

        return consumerBean;
    }
}
