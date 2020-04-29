package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.coua.common.SubscriptionState;
import com.keepreal.madagascar.coua.dao.SubscriptionRepository;
import com.keepreal.madagascar.coua.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Service
public class SubscriptionService {

    public static final int HOST_NUMBER = 1;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public void initHost(Long islandId, Long hostId) {
        Subscription subscription = new Subscription();
        subscription.setIslandId(islandId);
        subscription.setUserId(hostId);
        subscription.setIslanderNumber(HOST_NUMBER);
        subscription.setState(SubscriptionState.HOST.getValue());
        subscription.setCreatedTime(System.currentTimeMillis());
        subscription.setUpdatedTime(System.currentTimeMillis());
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

    public void subscribeIsland(Long islandId, Long userId) {
        Subscription subscription = subscriptionRepository.getSubscriptionByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        // 如果这个用户之前加入过这个岛，那么只需要恢复他的状态即可
        if (subscription != null) {
            updateSubscriptionState(subscription, SubscriptionState.ISLANDER);
        } else {
            //创建对象
            subscription = new Subscription();
            subscription.setIslandId(islandId);
            subscription.setUserId(userId);
            subscription.setState(SubscriptionState.ISLANDER.getValue());
            subscription.setCreatedTime(System.currentTimeMillis());
            subscription.setUpdatedTime(System.currentTimeMillis());

            //查询上一个number，+1作为本条记录的number todo 如果先查后写，这样会有并发问题

        }
        subscriptionRepository.save(subscription);
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
