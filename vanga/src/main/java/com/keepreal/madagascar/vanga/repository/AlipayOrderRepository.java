package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.AlipayOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents the alipay order repository.
 */
@Repository
public interface AlipayOrderRepository extends JpaRepository<AlipayOrder, String> {

    AlipayOrder findByIdAndDeletedIsFalse(String id);

    AlipayOrder findTopByTradeNumberAndDeletedIsFalse(String tradeNumber);

    @Modifying
    @Transactional
    @Query(value = "UPDATE alipay_order SET user_id = ?1 WHERE user_id = ?2", nativeQuery = true)
    void mergeUserAlipayOrder(String wechatUserId, String webMobileUserId);

}
