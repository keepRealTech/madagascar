package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.coua.common.IslandState;
import com.keepreal.madagascar.coua.dao.SubscriptionRepository;
import com.keepreal.madagascar.coua.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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
        subscription.setNumber(HOST_NUMBER);
        subscription.setState(IslandState.HOST.getValue());
        subscription.setCreateTime(System.currentTimeMillis());
        subscription.setUpdateTime(System.currentTimeMillis());
        subscriptionRepository.save(subscription);
    }

    public Page<Long> getIslandIdListByUserCreated(Long userId, Pageable pageable) {
        int state = IslandState.HOST.getValue();
        return getIslandsByUserState(userId, state, pageable);
    }

    public Page<Long> getIslandIdListByUserSubscribed(Long userId, Pageable pageable) {
        int state = IslandState.ISLANDER.getValue();
        return getIslandsByUserState(userId, state, pageable);
    }

    public Page<Long> getSubscriberIdListByIslandId(Long islandId, Pageable pageable) {
        return subscriptionRepository.getSubscriberIdListByIslandId(islandId, pageable);
    }

    private Page<Long> getIslandsByUserState(Long userId, Integer state, Pageable pageable) {
        return subscriptionRepository.getIslandIdListByUserState(userId, state, pageable);
    }

    public void subscribeIsland(Long islandId, Long userId) {
        //创建对象
        Subscription subscription = new Subscription();
        subscription.setIslandId(islandId);
        subscription.setUserId(userId);
        subscription.setState(IslandState.ISLANDER.getValue());
        subscription.setCreateTime(System.currentTimeMillis());
        subscription.setUpdateTime(System.currentTimeMillis());

        //查询上一个number，+1作为本条记录的number todo 如果先查后写，这样会有并发问题

        //保存
        subscriptionRepository.save(subscription);
    }
}
