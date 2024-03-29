package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "SELECT user_id FROM subscription WHERE island_id = ?1 AND state = ?2 AND is_deleted = FALSE", nativeQuery = true)
    Long getIslandOwnerId(String islandId, Integer state);

    @Query(value = "SELECT island_id FROM subscription WHERE user_id = ?1 AND state = ?2 AND is_deleted = FALSE", nativeQuery = true)
    Page<String> getIslandIdListByUserState(String userId, Integer state, Pageable pageable);

    @Query(value = "SELECT island_id FROM subscription WHERE user_id = ?1 AND state > 0 AND is_deleted = FALSE ORDER BY state ASC, created_time DESC", nativeQuery = true)
    Page<String> getIslandIdListByUserSubscribed(String userId, Pageable pageable);

    @Query(value = "SELECT user_id FROM subscription WHERE island_id = ?1 AND state > 0 AND is_deleted = FALSE ORDER BY state ASC, created_time ASC", nativeQuery = true)
    Page<String> getSubscriberIdListByIslandId(String islandId, Pageable pageable);

    @Query(value = "SELECT user_id FROM subscription WHERE island_id = ?1 AND state > 1 AND is_deleted = FALSE ORDER BY state ASC, created_time ASC", nativeQuery = true)
    Page<String> getIslanderIdListByIslandId(String islandId, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM subscription WHERE island_id = ?1 AND state > 0 AND is_deleted = FALSE", nativeQuery = true)
    Integer getCountByIslandId(String islandId);

    @Query(value = "SELECT islander_number FROM subscription WHERE island_id = ?1 AND user_id = ?2 AND is_deleted = FALSE AND state > 0", nativeQuery = true)
    Integer getIslanderNumberByIslandId(String islandId, String userId);

    Subscription findTopByIslandIdAndUserIdAndDeletedIsFalse(String islandId, String userId);

    @Query(value = "SELECT island_id FROM subscription WHERE user_id = ?1 AND island_id IN ?2 AND state > 0 AND is_deleted = FALSE", nativeQuery = true)
    List<String> getIslandIdListByUserSubscribedIn(String userId, List<String> islandIdList);

    @Query(value = "SELECT island_id FROM user u LEFT JOIN subscription s ON u.id = s.user_id WHERE s.state = 1 AND u.is_deleted = FALSE AND u.nick_name LIKE ?1%", nativeQuery = true)
    Page<String> getIslandIdsByUsername(String username, Pageable pageable);

}
