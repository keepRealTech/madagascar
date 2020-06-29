package com.keepreal.madagascar.mantella.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.OrderConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.keepreal.madagascar.mantella.consumer.FeedEventListener;
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
@ConfigurationProperties(prefix = "rocketmq.feed", ignoreUnknownFields = false)
@Data
public class FeedEventConsumerConfiguration {

    private final FeedEventListener feedEventListener;
    private String accessKey;
    private String secretKey;
    private String nameSrvAddr;
    private String topic;
    private String groupId;
    private String tag;

    /**
     * Constructs the consumer bean {@link OrderConsumerBean}.
     *
     * @return {@link OrderConsumerBean}.
     */
    @Bean(name = "feed-event-consumer", initMethod = "start", destroyMethod = "shutdown")
    public OrderConsumerBean buildConsumer() {
        OrderConsumerBean orderConsumerBean = new OrderConsumerBean();

        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        properties.setProperty(PropertyKeyConst.GROUP_ID, this.getGroupId());
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        orderConsumerBean.setProperties(properties);

        Map<Subscription, MessageOrderListener> subscriptionTable = new HashMap<>();
        Subscription subscription = new Subscription();
        subscription.setTopic(this.getTopic());
        subscription.setExpression(this.getTag());
        subscriptionTable.put(subscription, this.feedEventListener);
        orderConsumerBean.setSubscriptionTable(subscriptionTable);

        return orderConsumerBean;
    }

}
