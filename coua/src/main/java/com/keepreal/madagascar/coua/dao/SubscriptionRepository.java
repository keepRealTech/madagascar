package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query(value = "SELECT user_id FROM subscription WHERE island_id = ?1 AND state = ?2 AND is_deleted = FALSE", nativeQuery = true)
    Long getIslandOwnerId(Long islandId, Integer state);

    @Query(value = "SELECT island_id FROM subscription WHERE user_id = ?1 AND state = ?2 AND is_deleted = FALSE", nativeQuery = true)
    Page<Long> getIslandIdListByUserState(Long userId, Integer state, Pageable pageable);

    @Query(value = "SELECT user_id FROM subscription WHERE island_id = ?1 AND is_deleted = FALSE", nativeQuery = true)
    Page<Long> getSubscriberIdListByIslandId(Long islandId, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM subscription WHERE island_id = ?1 AND state > 0 AND is_deleted = FALSE", nativeQuery = true)
    Integer getCountByIslandId(Long islandId);

    @Query(value = "SELECT number FROM subscription WHERE island_id = ?1 AND user_id = ?2 AND is_deleted = FALSE", nativeQuery = true)
    Integer getIslanderNumberByIslandId(Long islandId, Long userId);

    Subscription getSubscriptionByIslandIdAndUserIdAndDeletedIsFalse(Long islandId, Long userId);

}
