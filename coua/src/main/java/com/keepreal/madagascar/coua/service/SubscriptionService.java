package com.keepreal.madagascar.coua.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.common.SubscriptionState;
import com.keepreal.madagascar.coua.config.NotificationEventProducerConfiguration;
import com.keepreal.madagascar.coua.dao.IslandInfoRepository;
import com.keepreal.madagascar.coua.dao.SubscriptionRepository;
import com.keepreal.madagascar.coua.model.Subscription;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import com.keepreal.madagascar.tenrecs.SubscribeEvent;
import com.keepreal.madagascar.tenrecs.UnsubscribeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents subscription service.
 */
@Slf4j
@Service
public class SubscriptionService {

    private static final int HOST_NUMBER = 1;
    private static final int SUPER_ADMIN_NUMBER = 2;
    private static final String SUPER_ADMIN_USER_ID = "99999999";

    private final SubscriptionRepository subscriptionRepository;
    private final LongIdGenerator idGenerator;
    private final ProducerBean producerBean;
    private final NotificationEventProducerConfiguration notificationEventProducerConfiguration;
    private final IslandInfoRepository islandInfoRepository;

    /**
     * Constructs subscription service.
     *
     * @param subscriptionRepository {@link SubscriptionRepository}.
     * @param idGenerator            {@link LongIdGenerator}.
     * @param producerBean           {@link ProducerBean}.
     * @param notificationEventProducerConfiguration               {@link NotificationEventProducerConfiguration}.
     * @param islandInfoRepository   {@link IslandInfoRepository}.
     */
    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               LongIdGenerator idGenerator,
                               @Qualifier("notification-event-producer") ProducerBean producerBean,
                               NotificationEventProducerConfiguration notificationEventProducerConfiguration,
                               IslandInfoRepository islandInfoRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.idGenerator = idGenerator;
        this.producerBean = producerBean;
        this.notificationEventProducerConfiguration = notificationEventProducerConfiguration;
        this.islandInfoRepository = islandInfoRepository;
    }

    /**
     * When create island, insert host info.
     *
     * @param islandId islandId.
     * @param hostId   hostId.
     */
    public void initHost(String islandId, String hostId) {
        Subscription subscription = Subscription.builder()
                .id(String.valueOf(idGenerator.nextId()))
                .islandId(islandId)
                .userId(hostId)
                .islanderNumber(HOST_NUMBER)
                .state(SubscriptionState.HOST.getValue())
                .build();
        insertSubscription(subscription);

        Subscription adminSubscription = Subscription.builder()
                .id(String.valueOf(idGenerator.nextId()))
                .islandId(islandId)
                .userId(SubscriptionService.SUPER_ADMIN_USER_ID)
                .islanderNumber(SubscriptionService.SUPER_ADMIN_NUMBER)
                .state(SubscriptionState.SUPER_ADMIN.getValue())
                .build();
        insertSubscription(adminSubscription);
    }

    /**
     * if user subscribed the island
     *
     * @param islandId islandId
     * @param userId   userId
     * @return isSubscribed
     */
    public boolean isSubScribedIsland(String islandId, String userId) {
        Subscription subscription = subscriptionRepository.findTopByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        return subscription != null && subscription.getState() > 0;
    }

    /**
     * Retrieve pageable islandIdList by user created.
     *
     * @param userId   userId.
     * @param pageable {@link Pageable}.
     * @return islandIdList.
     */
    public Page<String> getIslandIdListByUserCreated(String userId, Pageable pageable) {
        int state = SubscriptionState.HOST.getValue();
        return getIslandsByUserState(userId, state, pageable);
    }

    /**
     * Retrieve pageable islandIdList by user create and subscribe.
     *
     * @param userId   userId.
     * @param pageable {@link Pageable}.
     * @return islandIdList
     */
    public Page<String> getIslandIdListByUserSubscribed(String userId, Pageable pageable) {
        return subscriptionRepository.getIslandIdListByUserSubscribed(userId, pageable);
    }

    /**
     * Retrieve pageable userIdList by subscribe the island (with island host).
     *
     * @param islandId islandId.
     * @param pageable {@link Pageable}.
     * @return userIdList.
     */
    public Page<String> getSubscriberIdListByIslandId(String islandId, Pageable pageable) {
        return subscriptionRepository.getSubscriberIdListByIslandId(islandId, pageable);
    }

    /**
     * Retrieve pageable userIdList by subscribe the island (without island host).
     *
     * @param islandId islandId.
     * @param pageable {@link Pageable}.
     * @return userIdList.
     */
    public Page<String> getIslanderIdListByIslandId(String islandId, Pageable pageable) {
        return subscriptionRepository.getIslanderIdListByIslandId(islandId, pageable);
    }

    /**
     * Retrieve island member count.
     *
     * @param islandId islandId.
     * @return member count.
     */
    public Integer getMemberCountByIslandId(String islandId) {
        return subscriptionRepository.getCountByIslandId(islandId);
    }

    /**
     * Retrieve user index in this island.
     *
     * @param islandId islandId.
     * @param userId   userId.
     * @return user index.
     */
    public Subscription getSubscriptionByIslandIdAndUserId(String islandId, String userId) {
        return subscriptionRepository.findTopByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
    }

    /**
     * Retrieve islandIdList by user subscription state.
     *
     * @param userId   userId.
     * @param state    subscription state.
     * @param pageable {@link Pageable}.
     * @return islandIdList.
     */
    private Page<String> getIslandsByUserState(String userId, Integer state, Pageable pageable) {
        return subscriptionRepository.getIslandIdListByUserState(userId, state, pageable);
    }

    /**
     * Subscribe island and send mq message.
     *
     * @param islandId       islandId.
     * @param userId         userId.
     * @param hostId         hostId.
     * @param islanderNumber islandNumber.
     */
    public void subscribeIsland(String islandId, String userId, String hostId, Integer islanderNumber) {
        Subscription subscription = subscriptionRepository.findTopByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        // 如果这个用户之前加入过这个岛，那么只需要恢复他的状态即可
        if (subscription != null) {
            subscription.setState(SubscriptionState.ISLANDER.getValue());
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
        insertSubscription(subscription);

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
        Message message = new Message(notificationEventProducerConfiguration.getTopic(), notificationEventProducerConfiguration.getTag(), event.toByteArray());
        message.setKey(uuid);
        producerBean.sendAsync(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
            }

            @Override
            public void onException(OnExceptionContext context) {
                log.error("this message send failure, message Id is {}", context.getMessageId());
            }
        });
    }

    /**
     * Unsubscribe island.
     *
     * @param islandId islandId.
     * @param userId   userId.
     */
    public void unsubscribeIsland(String islandId, String userId) {
        Subscription subscription = subscriptionRepository.findTopByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        if (subscription == null) {
            return;
        }

        subscription.setState(SubscriptionState.LEAVE.getValue());
        subscriptionRepository.save(subscription);

        String uuid = UUID.randomUUID().toString();
        UnsubscribeEvent unsubscribeEvent = UnsubscribeEvent.newBuilder()
                .setIslandId(islandId)
                .setSubscriberId(userId)
                .build();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_UNSUBSCRIBE)
                .setUserId(userId)
                .setUnsubscribeEvent(unsubscribeEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        Message message = new Message(notificationEventProducerConfiguration.getTopic(), notificationEventProducerConfiguration.getTag(), event.toByteArray());
        message.setKey(uuid);
        producerBean.sendAsync(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
            }

            @Override
            public void onException(OnExceptionContext context) {
                log.error("this message send failure, message Id is {}", context.getMessageId());
            }
        });
    }

    public List<String> getSubscribeIslandIdByUserId(String userId, List<String> islandIdList) {
        return subscriptionRepository.getIslandIdListByUserSubscribedIn(userId, islandIdList);
    }

    public boolean isSubscribed(String userId, String islandId) {
        Subscription subscription = subscriptionRepository.findTopByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        return subscription != null && subscription.getState() > 0;
    }

    /**
     * Flips the island host and normal user should introduce to false.
     *
     * @param userId   User id.
     * @param islandId Island id.
     */
    public void dismissHostAndUserIntroduction(String userId, String islandId) {
        Subscription subscription = this.subscriptionRepository.findTopByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        if (Objects.isNull(subscription)) {
            return;
        }

        subscription.setShouldIntroduce(false);
        this.updateSubcription(subscription);
    }

    /**
     * Update subscription
     *
     * @param subscription {@link Subscription}
     * @return {@link Subscription}
     */
    public Subscription updateSubcription(Subscription subscription) {
        return this.subscriptionRepository.save(subscription);
    }

    public Page<String> getIslandIdsByUsername(String username, Pageable pageable) {
        return this.subscriptionRepository.getIslandIdsByUsername(username, pageable);
    }

    private void insertSubscription(Subscription subscription) {
        try {
            subscriptionRepository.save(subscription);
        } catch (DataIntegrityViolationException e) {
            log.error("[insertSubscription] subscription sql duplicate error! subscription id is [{}], island id and user id is [{}, {}]",
                    subscription.getId(), subscription.getIslandId(), subscription.getUserId());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_SUBSCRIPTION_SQL_DUPLICATE_ERROR);
        }
    }
}
