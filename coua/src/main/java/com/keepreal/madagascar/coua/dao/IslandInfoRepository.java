package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.IslandInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Repository
public interface IslandInfoRepository extends JpaRepository<IslandInfo, String> {

    IslandInfo findTopByIslandNameAndDeletedIsFalse(String islandName);

    @Query(value = "SELECT id, last_feed_at AS lastFeedAt FROM island WHERE is_deleted = FALSE AND id IN ?1", nativeQuery = true)
    List<Map<String, Long>> findIslandIdAndLastFeedAtByIslandIdList(List<String> islandIdList);

    /**
     * 根据islandIdList更新lastFeedAt字段
     * todo: 会有性能问题，以后要改掉
     * @param islandIdList
     * @param timestamp
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE island SET last_feed_at = ?2 WHERE id IN ?1", nativeQuery = true)
    void updateLastFeedAtByIslandIdList(List<String> islandIdList, Long timestamp);

    @Query(value = "SELECT islander_number FROM island WHERE id = ?1 FOR UPDATE", nativeQuery = true)
    Integer getIslanderNumberByIslandId(String islandId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE island SET islander_number = islander_number + 1 WHERE id = ?1", nativeQuery = true)
    void updateIslanderNumberById(String islandId);

    List<IslandInfo> findIslandInfosByIdInAndDeletedIsFalse(List<String> idList);
}
