package com.keepreal.madagascar.workflow.support_activity.repository.vanga;

import com.keepreal.madagascar.workflow.support_activity.model.vanga.SupportActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportActivityRepository extends JpaRepository<SupportActivity, String> {

    /**
     * created_time: 1599494400000 ==> 2020-09-08 00:00:00
     *               1602086400000 ==> 2020-10-20 24:00:00
     */
    @Query(value = "SELECT DISTINCT payee_id " +
            "FROM balance_log " +
            "WHERE created_time > 1599494400000 AND created_time < 1603209600000 AND (state = 2 OR state = 3) AND is_deleted = 0",
            nativeQuery = true)
    List<String> findAllPayeeId();

    /**
     * created_time: 1599494400000 ==> 2020-09-08 00:00:00
     *               1602086400000 ==> 2020-10-20 24:00:00
     *
     * @param userId payeeId.
     * @return max amount
     */
    @Query(value = "SELECT max(amount_in_cents) AS amount " +
            "FROM balance_log " +
            "WHERE payee_id = ?1 AND created_time > 1599494400000 AND created_time < 1603209600000 AND (state = 2 OR state = 3) AND is_deleted = 0 " +
            "GROUP BY user_id",
            nativeQuery = true)
    List<Long> findByUserId(String userId);

    SupportActivity findSupportActivityByUserIdAndDeletedIsFalse(String userId);
}
