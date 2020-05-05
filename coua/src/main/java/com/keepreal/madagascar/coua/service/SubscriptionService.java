package com.keepreal.madagascar.coua.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.NotificationEvent;
import com.keepreal.madagascar.coua.NotificationEventType;
import com.keepreal.madagascar.coua.SubscribeEvent;
import com.keepreal.madagascar.coua.common.SubscriptionState;
import com.keepreal.madagascar.coua.config.MqConfig;
import com.keepreal.madagascar.coua.dao.SubscriptionRepository;
import com.keepreal.madagascar.coua.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Service
public class SubscriptionService {

    public static final int HOST_NUMBER = 1;

    private final SubscriptionRepository subscriptionRepository;
    private final LongIdGenerator idGenerator;
    private final ProducerBean producerBean;
    private final MqConfig mqConfig;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, LongIdGenerator idGenerator, ProducerBean producerBean, MqConfig mqConfig) {
        this.subscriptionRepository = subscriptionRepository;
        this.idGenerator = idGenerator;
        this.producerBean = producerBean;
        this.mqConfig = mqConfig;
    }

    public void initHost(Long islandId, Long hostId) {
        Subscription subscription = new Subscription();
        subscription.setId(idGenerator.nextId());
        subscription.setIslandId(islandId);
        subscription.setUserId(hostId);
        subscription.setIslanderNumber(HOST_NUMBER);
        subscription.setState(SubscriptionState.HOST.getValue());
        subscriptionRepository.save(subscription);
    }

    public Page<Long> getIslandIdListByUserCreated(Long userId, Pageable pageable) {
        int state = SubscriptionState.HOST.getValue();
        return getIslandsByUserState(userId, state, pageable);
    }

    public Page<Long> getIslandIdListByUserSubscribed(Long userId, Pageable pageable) {
        int state = SubscriptionState.ISLANDER.getValue();
        return getIslandsByUserState(userId, state, pageable);
    }

    public Page<Long> getSubscriberIdListByIslandId(Long islandId, Pageable pageable) {
        return subscriptionRepository.getSubscriberIdListByIslandId(islandId, pageable);
    }

    public Integer getMemberCountByIslandId(Long islandId) {
        return subscriptionRepository.getCountByIslandId(islandId);
    }

    public Integer getUserIndexByIslandId(Long islandId, Long userId) {
        return subscriptionRepository.getIslanderNumberByIslandId(islandId, userId);
    }

    private Page<Long> getIslandsByUserState(Long userId, Integer state, Pageable pageable) {
        return subscriptionRepository.getIslandIdListByUserState(userId, state, pageable);
    }

    public void subscribeIsland(Long islandId, Long userId, Integer islanderNumber) {
        Subscription subscription = subscriptionRepository.getSubscriptionByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        // 如果这个用户之前加入过这个岛，那么只需要恢复他的状态即可
        if (subscription != null) {
            updateSubscriptionState(subscription, SubscriptionState.ISLANDER);
        } else {
            //创建对象
            subscription = new Subscription();
            subscription.setId(idGenerator.nextId());
            subscription.setIslandId(islandId);
            subscription.setUserId(userId);
            subscription.setState(SubscriptionState.ISLANDER.getValue());
            subscription.setIslanderNumber(islanderNumber);
        }
        subscriptionRepository.save(subscription);

        //向mq发消息
        SubscribeEvent subscribeEvent = SubscribeEvent.newBuilder()
                .setIslandId(islandId.toString())
                .setSubscriberId(userId.toString())
                .build();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_SUBSCRIBE)
                .setUserId(userId.toString())
                .setSubscribeEvent(subscribeEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(UUID.randomUUID().toString())
                .build();
        Message message = new Message(mqConfig.getTopic(), mqConfig.getTag(), event.toByteArray());
        producerBean.send(message);
    }

    public void unSubscribeIsland(Long islandId, Long userId) {
        Subscription subscription = subscriptionRepository.getSubscriptionByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        if (subscription != null) {
            updateSubscriptionState(subscription, SubscriptionState.LEAVE);
            subscriptionRepository.save(subscription);
        }
    }

    private void updateSubscriptionState(Subscription subscription, SubscriptionState state) {
        subscription.setState(state.getValue());
        subscription.setUpdatedTime(System.currentTimeMillis());
    }
}
