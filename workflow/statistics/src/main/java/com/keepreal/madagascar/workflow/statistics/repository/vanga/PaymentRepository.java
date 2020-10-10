package com.keepreal.madagascar.workflow.statistics.repository.vanga;

import com.keepreal.madagascar.workflow.statistics.model.vanga.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Query(value = "SELECT distinct COUNT(DISTINCT user_id) FROM balance_log WHERE created_time > ?1 AND created_time < ?2 AND (state = 2 OR state = 3)", nativeQuery = true)
    Integer countUserYesterday(long startTimestamp, long endTimestamp);

    @Query(value = "SELECT distinct COUNT(DISTINCT user_id) FROM balance_log WHERE state = 2 OR state = 3", nativeQuery = true)
    Integer countUserTotal();

    @Query(value = "SELECT distinct payee_id FROM balance_log WHERE created_time > ?1 AND created_time < ?2 AND (state = 2 OR state = 3)", nativeQuery = true)
    Integer countCreaterYesterday(long startTimestamp, long endTimestamp);

    @Query(value = "SELECT distinct payee_id FROM balance_log WHERE state = 2 OR state = 3", nativeQuery = true)
    Integer countCreaterTotal();

    Integer countYesterdayByPayeeId(String payeeId, long startTimestamp, long endTimestamp);

    Integer countAmountYesterdayByPayeeId(String payeeId, long startTimestamp, long endTimestamp);
}
