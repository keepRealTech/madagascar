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
public interface IslandInfoRepository extends JpaRepository<IslandInfo, Long> {

    IslandInfo findByIslandNameAndDeletedIsFalse(String islandName);

    @Query(value = "SELECT island_id, last_feed_at FROM island WHERE is_deleted = FALSE AND id IN ?1", nativeQuery = true)
    Map<Long, Long> findIslandIdAndLastFeedAtByIslandIdList(List<Long> islandIdList);

    @Modifying
    @Transactional
    @Query(value = "UPDATE island SET last_feed_at = ?2 WHERE id IN ?1", nativeQuery = true)
    void updateLastFeedAtByIslandIdList(List<Long> islandIdList, Long timestamp);
}
