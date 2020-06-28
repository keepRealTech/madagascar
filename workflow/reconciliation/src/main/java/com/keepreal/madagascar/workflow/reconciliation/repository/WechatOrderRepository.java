package com.keepreal.madagascar.workflow.reconciliation.repository;

import com.keepreal.madagascar.workflow.reconciliation.model.WechatOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WechatOrderRepository extends JpaRepository<WechatOrder, String> {

    WechatOrder findTopByTradeNumberAndDeletedIsFalse(String tradeNumber);

}