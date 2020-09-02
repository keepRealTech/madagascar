package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.WechatOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface WechatOrderRepository extends JpaRepository<WechatOrder, String> {

    WechatOrder findByIdAndDeletedIsFalse(String id);

    WechatOrder findTopByTradeNumberAndDeletedIsFalse(String tradeNumber);

    WechatOrder findByPropertyIdAndTypeAndDeletedIsFalse(String propertyId, Integer type);

    @Modifying
    @Transactional
    @Query(value = "UPDATE wechat_order SET user_id = ?1 WHERE user_id = ?2", nativeQuery = true)
    void mergeUserWechatOrder(String wechatUserId, String webMobileUserId);

}