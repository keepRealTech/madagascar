package com.keepreal.madagascar.workflow.statistics.repository;

import com.keepreal.madagascar.workflow.statistics.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    @Query(value = "SELECT island_id as islandId, count(1) as increment FROM Subscription WHERE is_deleted=0 AND created_time >= ?1 " +
            "AND created_time <= ?2 GROUP BY island_id HAVING count(1) > ?3",
            nativeQuery = true)
    List<Object[]> findHitIslandIds(long startTimestamp, long endTimestamp, int threshold);

}