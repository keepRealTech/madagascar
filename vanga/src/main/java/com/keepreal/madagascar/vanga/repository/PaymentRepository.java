package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Represents the payment repository.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findAllByTypeAndCreatedTimeAfterAndDeletedIsFalse(Integer type, Long timestampAfter);

    List<Payment> findAllByOrderIdAndDeletedIsFalse(String orderId);

    Payment findTopByTradeNumAndTypeAndDeletedIsFalse(String tradeNum, Integer type);

    @Query(value = "SELECT min(id) as id, user_id, payee_id, trade_num, amount_in_cents, amount_in_shells, withdraw_percent, " +
            "membership_sku_id, order_id, type, state, max(valid_after) as valid_after, is_deleted, min(created_time) as created_time, " +
            "min(updated_time) as updated_time FROM balance_log where (type=1 OR type=3 OR type=5) AND (state=2 OR state=3) AND is_deleted=0 " +
            "AND user_id=?1 GROUP BY trade_num",
           countQuery = "SELECT COUNT(1) FROM (SELECT trade_num FROM balance_log where (type=1 OR type=3 OR type=5) AND (state=2 OR state=3) " +
                   "AND is_deleted=0 AND user_id=?1 GROUP BY trade_num) AS groups",
           nativeQuery = true)
    Page<Payment> findAllValidPaymentsByUserId(String userId, Pageable pageable);

    Integer countByPayeeIdAndTypeAndStateInAndDeletedIsFalse(String userId, int type, List<Integer> state);
    @Modifying
    @Transactional
    @Query(value = "UPDATE balance_log SET user_id = ?1 WHERE user_id = ?2", nativeQuery = true)
    void mergeUserPayment(String wechatUserId, String webMobileUserId);

}
