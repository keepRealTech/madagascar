package com.keepreal.madagascar.workflow.supportActivity.repository;

import com.keepreal.madagascar.workflow.supportActivity.model.SupportActivity;
import com.keepreal.madagascar.workflow.supportActivity.model.MaxAmount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportActivityRepository extends JpaRepository<SupportActivity, String> {

    @Query(value = "SELECT DISTINCT payee_id " +
            "FROM balance_log " +
            "WHERE membership_sku_id != '' AND created_time > 1599494400000 AND created_time < 1602086400000 AND (state = 2 OR state = 3)",
            nativeQuery = true)
    List<String> findAllPayeeId();

    @Query(value = "SELECT max(amount_in_cents) AS amount " +
            "FROM balance_log " +
            "WHERE membership_sku_id != '' AND payee_id = ?1 AND created_time > 1599494400000 AND created_time < 1602086400000 AND (state = 2 OR state = 3)" +
            "GROUP BY user_id",
            nativeQuery = true)
    List<MaxAmount> findByUserId(String userId);

    SupportActivity findSupportActivityByUserIdAndDeletedIsFalse(String userId);
}
