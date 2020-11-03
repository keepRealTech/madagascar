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
            "min(updated_time) as updated_time FROM balance_log where (type=1 OR type=3 OR type=5 OR type=7) AND (state=2 OR state=3) AND is_deleted=0 " +
            "AND user_id=?1 GROUP BY trade_num",
            countQuery = "SELECT COUNT(1) FROM (SELECT trade_num FROM balance_log where (type=1 OR type=3 OR type=5 OR type=7) AND (state=2 OR state=3) " +
                    "AND is_deleted=0 AND user_id=?1 GROUP BY trade_num) AS groups",
            nativeQuery = true)
    Page<Payment> findAllValidPaymentsByUserId(String userId, Pageable pageable);

    Page<Payment> findAllByTypeAndUserIdAndDeletedIsFalse(Integer type, String userId, Pageable pageable);

    Integer countByPayeeIdAndTypeAndStateInAndDeletedIsFalse(String userId, int type, List<Integer> state);

    @Modifying
    @Transactional
    @Query(value = "UPDATE balance_log SET user_id = ?1 WHERE user_id = ?2", nativeQuery = true)
    void mergeUserPayment(String wechatUserId, String webMobileUserId);

    /**
     * 计算创作者在指定时间范围内付费的用户数量
     */
    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    Integer countSupportCountByPayeeIdAndTimestamp(String payeeId, long startTimestamp, long endTimestamp);

    /**
     * 计算创作者在指定时间范围内的收入总和
     */
    @Query(value = "SELECT SUM(amount_in_cents) FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    Long countAmountByPayeeIdAndTimestamp(String payeeId, long startTimestamp, long endTimestamp);

    /**
     * 计算创作者在指定时间范围内付费用户支持一下的次数（type=6 用来筛选支持一下的记录）
     */
    @Query(value = "SELECT COUNT(*) FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type = 6", nativeQuery = true)
    Integer countSponsorByPayeeIdAndTimestamp(String payeeId, long startTimestamp, long endTimestamp);

    /**
     * 计算创作者在指定时间范围内付费用户支持一下的收入总和（type=6 用来筛选支持一下的记录）
     */
    @Query(value = "SELECT SUM(amount_in_cents) FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type = 6", nativeQuery = true)
    Long countSponsorIncomeByPayeeIdAndTimestamp(String payeeId, long startTimestamp, long endTimestamp);

    /**
     * 计算创作者在指定时间范围内付费用户的数量
     * (valid_after是表示结算时间的字段，比如用户买了三个月会员，会有三条记录，created_time相同，而valid_after不同)
     */
    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM balance_log WHERE payee_id = ?1 AND valid_after > ?2 AND valid_after < ?3 AND (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    Integer countSupportCountByPayeeIdAndValidAfter(String payeeId, long startTimestamp, long endTimestamp);

    /**
     * 计算创作者在指定时间范围内的收入总和
     * (valid_after是表示结算时间的字段，比如用户买了三个月会员，会有三条记录，created_time相同，而valid_after不同)
     */
    @Query(value = "SELECT SUM(amount_in_cents) FROM balance_log WHERE payee_id = ?1 AND valid_after > ?2 AND valid_after < ?3 AND (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    Long countAmountByPayeeIdAndValidAfter(String payeeId, long startTimestamp, long endTimestamp);

    /**
     * 计算创作者在指定时间范围内购买指定会员的付费用户数量
     */
    @Query(value = "SELECT COUNT(DISTINCT order_id) FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) ", nativeQuery = true)
    Integer countSupportCountByPayeeIdAndTimestampAndMemberhipSku(String payeeId, long startTimestamp, long endTimestamp, List<String> membershipSkuIds);

    /**
     * 计算创作者在指定时间范围内购买指定会员的收入总和
     */
    @Query(value = "SELECT SUM(amount_in_cents) FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND membership_sku_id IN ?4", nativeQuery = true)
    Long countAmountByPayeeIdAndTimestampAndMembershipSku(String payeeId, long startTimestamp, long endTimestamp, List<String> membershipSkuIds);

    /**
     * 计算创作者在指定时间范围内的记录
     */
    @Query(value = "SELECT id, user_id, payee_id, trade_num, " +
            "amount_in_cents, amount_in_shells, order_id, " +
            "type, state, valid_after, is_deleted, created_time, updated_time," +
            "withdraw_percent, membership_sku_id " +
            "FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type != 4",
            countQuery = "select count(*) from balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type != 4",
            nativeQuery = true)
    Page<Payment> retrievePaymentsByPayeeIdAndTimestamp(String payeeId, long startTimestamp, long endTimestamp, Pageable pageable);

    /**
     * 计算创作者在指定时间范围内购买指定会员的记录
     */
    @Query(value = "SELECT id, user_id, payee_id, trade_num, " +
            "amount_in_cents, amount_in_shells, order_id, " +
            "type, state, valid_after, is_deleted, created_time, updated_time," +
            "withdraw_percent, membership_sku_id " +
            "FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND membership_sku_id IN ?4 GROUP BY order_id",
            countQuery = "select count(*) from balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type != 4",
            nativeQuery = true)
    Page<Payment> retrieveMembershipPaymentsByPayeeIdAndTimestamp(String payeeId, long startTimestamp, long endTimestamp, List<String> membershipSkuIds, Pageable pageable);

    /**
     * 计算创作者在指定时间范围内付费用户支持一下的记录（type=6 用来筛选支持一下的记录）
     */
    @Query(value = "SELECT id, user_id, payee_id, trade_num, " +
            "amount_in_cents, amount_in_shells, order_id, " +
            "type, state, valid_after, is_deleted, created_time, updated_time," +
            "withdraw_percent, membership_sku_id " +
            "FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type = 6",
            countQuery = "select count(*) from balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type != 4",
            nativeQuery = true)
    Page<Payment> retrieveSponsorPaymentsByPayeeIdAndTimestamp(String payeeId, long startTimestamp, long endTimestamp, Pageable pageable);

    /**
     * 计算创作者在指定时间范围内支付单独解锁的记录
     */
    @Query(value = "SELECT id, user_id, payee_id, trade_num, " +
            "amount_in_cents, amount_in_shells, order_id, " +
            "type, state, valid_after, is_deleted, created_time, updated_time," +
            "withdraw_percent, membership_sku_id " +
            "FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND (type = 1 OR type = 7) AND membership_sku_id = ''",
            countQuery = "select count(*) from balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type != 4",
            nativeQuery = true)
    Page<Payment> retrieveFeedChargePaymentsByPayeeIdAndTimestamp(String payeeId, long startTimestamp, long endTimestamp, Pageable pageable);


}
