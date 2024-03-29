package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.IslandInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Repository
public interface IslandInfoRepository extends JpaRepository<IslandInfo, String> {

    IslandInfo findTopByIslandNameAndDeletedIsFalse(String islandName);

    List<IslandInfo> findAllByIdInAndDeletedIsFalse(Iterable<String> ids);

    Page<IslandInfo> findByIslandNameStartingWithAndDeletedIsFalse(String islandName, Pageable pageable);

    IslandInfo findTopByIdAndDeletedIsFalse(String id);

    Page<IslandInfo> findAllByDeletedIsFalse(Pageable pageable);

    Page<IslandInfo> findAllByHostIdAndDeletedIsFalse(String hostId, Pageable pageable);

    @Query(value = "SELECT id, last_feed_at AS lastFeedAt FROM island WHERE is_deleted = FALSE AND id IN ?1", nativeQuery = true)
    List<Map<String, Long>> findIslandIdAndLastFeedAtByIslandIdList(List<String> islandIdList);

    @Query(value = "SELECT id, last_works_feed_at AS lastWorksFeedAt FROM island WHERE is_deleted = FALSE AND id IN ?1", nativeQuery = true)
    List<Map<String, Long>> findIslandIdAndLastWorksFeedAtByIslandIdList(List<String> islandIdList);

    /**
     * 根据islandIdList更新lastFeedAt字段
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE island SET last_feed_at = ?2 WHERE id IN ?1", nativeQuery = true)
    void updateLastFeedAtByIslandIdList(List<String> islandIdList, Long timestamp);

    @Modifying
    @Transactional
    @Query(value = "UPDATE island SET last_works_feed_at = ?2 WHERE id IN ?1", nativeQuery = true)
    void updateLastWorksFeedAtByIslandIdList(List<String> islandIdList, Long timestamp);

    @Query(value = "SELECT islander_number FROM island WHERE id = ?1 FOR UPDATE", nativeQuery = true)
    Integer getIslanderNumberByIslandId(String islandId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE island SET islander_number = islander_number + 1 WHERE id = ?1", nativeQuery = true)
    void updateIslanderNumberById(String islandId);

    @Query(value =
            "SELECT id, host_id, island_name, portrait_image_uri, description, " +
                    "secret, state, islander_number, last_feed_at, last_works_feed_at, is_deleted, show_income, custom_url, " +
                    "locked_until, created_time, updated_time, identity_id, island_access_type " +
            "FROM island " +
            "WHERE id IN ?1 ORDER BY FIELD (id, ?1) ",
           nativeQuery = true)
    List<IslandInfo> findIslandInfosByIdInAndDeletedIsFalse(List<String> idList);

    List<IslandInfo> findByIdInAndDeletedIsFalse(Set<String> idList);

    List<IslandInfo> findIslandInfosByHostIdIn(List<String> userIds);

    IslandInfo findTopByCustomUrlAndDeletedIsFalse(String customUrl);
}
