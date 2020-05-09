package com.keepreal.madagascar.coua.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.dao.IslandInfoRepository;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import com.keepreal.madagascar.tenrecs.SubscribeEvent;
import com.keepreal.madagascar.coua.common.SubscriptionState;
import com.keepreal.madagascar.coua.config.MqConfig;
import com.keepreal.madagascar.coua.dao.SubscriptionRepository;
import com.keepreal.madagascar.coua.model.Subscription;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class SubscriptionService {

    public static final int HOST_NUMBER = 1;

    private final SubscriptionRepository subscriptionRepository;
    private final LongIdGenerator idGenerator;
    private final ProducerBean producerBean;
    private final MqConfig mqConfig;
    private final IslandInfoRepository islandInfoRepository;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, LongIdGenerator idGenerator, ProducerBean producerBean, MqConfig mqConfig, IslandInfoRepository islandInfoRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.idGenerator = idGenerator;
        this.producerBean = producerBean;
        this.mqConfig = mqConfig;
        this.islandInfoRepository = islandInfoRepository;
    }

    public void initHost(String islandId, String hostId) {
        Subscription subscription = Subscription.builder()
                .id(String.valueOf(idGenerator.nextId()))
                .islandId(islandId)
                .userId(hostId)
                .islanderNumber(HOST_NUMBER)
                .state(SubscriptionState.HOST.getValue())
                .build();
        subscriptionRepository.save(subscription);
    }

    public boolean isSubScribedIsland(String islandId, String userId) {
        Subscription subscription = subscriptionRepository.findTopByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        return subscription != null;
    }

    public Page<String> getIslandIdListByUserCreated(String userId, Pageable pageable) {
        int state = SubscriptionState.HOST.getValue();
        return getIslandsByUserState(userId, state, pageable);
    }

    public Page<String> getIslandIdListByUserSubscribed(String userId, Pageable pageable) {
        int state = SubscriptionState.ISLANDER.getValue();
        return getIslandsByUserState(userId, state, pageable);
    }

    public Page<String> getSubscriberIdListByIslandId(String islandId, Pageable pageable) {
        return subscriptionRepository.getSubscriberIdListByIslandId(islandId, pageable);
    }

    public Integer getMemberCountByIslandId(String islandId) {
        return subscriptionRepository.getCountByIslandId(islandId);
    }

    public Integer getUserIndexByIslandId(String islandId, String userId) {
        return subscriptionRepository.getIslanderNumberByIslandId(islandId, userId);
    }

    private Page<String> getIslandsByUserState(String userId, Integer state, Pageable pageable) {
        return subscriptionRepository.getIslandIdListByUserState(userId, state, pageable);
    }

    public void subscribeIsland(String islandId, String userId, String hostId, Integer islanderNumber) {
        Subscription subscription = subscriptionRepository.findTopByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        // 如果这个用户之前加入过这个岛，那么只需要恢复他的状态即可
        if (subscription != null) {
            updateSubscriptionState(subscription, SubscriptionState.ISLANDER);
        } else {
            //创建对象
            subscription = Subscription.builder()
                    .id(String.valueOf(idGenerator.nextId()))
                    .islandId(islandId)
                    .userId(userId)
                    .state(SubscriptionState.ISLANDER.getValue())
                    .islanderNumber(islanderNumber + 1)
                    .build();
            islandInfoRepository.updateIslanderNumberById(islandId);
        }
        subscriptionRepository.save(subscription);

        //向mq发消息
        String uuid = UUID.randomUUID().toString();
        SubscribeEvent subscribeEvent = SubscribeEvent.newBuilder()
                .setIslandId(islandId)
                .setSubscriberId(userId)
                .build();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_SUBSCRIBE)
                .setUserId(hostId) //当用户加入这个岛时，收到这个通知的是岛主
                .setSubscribeEvent(subscribeEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        Message message = new Message(mqConfig.getTopic(), mqConfig.getTag(), event.toByteArray());
        message.setKey(uuid);
//        producerBean.sendAsync(message, new SendCallback() {
//            @Override
//            public void onSuccess(SendResult sendResult) {
//            }
//
//            @Override
//            public void onException(OnExceptionContext context) {
//                log.error("this message send failure, message Id is {}", context.getMessageId());
//            }
//        });
    }

    public void unSubscribeIsland(String islandId, String userId) {
        Subscription subscription = subscriptionRepository.findTopByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
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
