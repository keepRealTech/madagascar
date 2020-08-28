package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.WechatOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WechatOrderRepository extends JpaRepository<WechatOrder, String> {

    WechatOrder findByIdAndDeletedIsFalse(String id);

    WechatOrder findTopByTradeNumberAndDeletedIsFalse(String tradeNumber);

    WechatOrder findByPropertyIdAndTypeAndDeletedIsFalse(String propertyId, Integer type);

}