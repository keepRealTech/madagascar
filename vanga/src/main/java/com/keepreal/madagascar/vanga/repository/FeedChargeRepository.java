package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.FeedCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedChargeRepository extends JpaRepository<FeedCharge, String> {

    FeedCharge findFeedChargeByUserIdAndFeedIdAndDeletedIsFalse(String userId, String feedId);

    @Query(value = "SELECT feed_id FROM feed_charge WHERE user_id = ?1 AND island_id = ?2 AND feed_created_at > ?3 ORDER BY feed_created_at DESC", nativeQuery = true)
    List<String> findFeedIdByUserIdAndIslandIdTimestampAfter(String userId, String islandId, Long timestamp);

    @Query(value = "SELECT feed_id FROM feed_charge WHERE user_id = ?1 AND island_id = ?2 AND feed_created_at < ?3 ORDER BY feed_created_at DESC", nativeQuery = true)
    List<String> findFeedIdByUserIdAndIslandIdTimestampBefore(String userId, String islandId, Long timestamp);

    @Query(value = "SELECT COUNT(*) FROM feed_charge WHERE host_id = ?1 AND created_time > ?2 AND created_time < ?3", nativeQuery = true)
    Integer countByHostIdAndTimestamp(String userId, long startTimestamp, long endTimestamp);

    @Query(value = "SELECT SUM(price_in_cents) FROM feed_charge WHERE host_id = ?1 AND created_time > ?2 AND created_time < ?3", nativeQuery = true)
    Long countAmountByHostIdAndTimestamp(String userId, long startTimestamp, long endTimestamp);
}
