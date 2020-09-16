package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.AlipayOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the alipay order repository.
 */
@Repository
public interface AlipayOrderRepository extends JpaRepository<AlipayOrder, String> {

    AlipayOrder findByIdAndDeletedIsFalse(String id);

    AlipayOrder findTopByTradeNumberAndDeletedIsFalse(String tradeNumber);

}
