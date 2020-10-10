package com.keepreal.madagascar.workflow.statistics.repository.vanga;

import com.keepreal.madagascar.workflow.statistics.model.vanga.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM balance_log WHERE created_time > ?1 AND created_time < ?2 AND (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    Integer countUserYesterday(long startTimestamp, long endTimestamp);

    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM balance_log WHERE (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    Integer countUserTotal();

    @Query(value = "SELECT DISTINCT payee_id FROM balance_log WHERE created_time > ?1 AND created_time < ?2 AND (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    List<String> countCreatorYesterday(long startTimestamp, long endTimestamp);

    @Query(value = "SELECT COUNT(DISTINCT payee_id) FROM balance_log WHERE (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    Integer countCreatorTotal();

    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    Integer countYesterdayByPayeeId(String payeeId, long startTimestamp, long endTimestamp);

    @Query(value = "SELECT SUM(amount_in_cents) FROM balance_log WHERE payee_id = ?1 AND created_time > ?2 AND created_time < ?3 AND (state = 2 OR state = 3) AND type != 4", nativeQuery = true)
    Integer countAmountYesterdayByPayeeId(String payeeId, long startTimestamp, long endTimestamp);
}
