package com.keepreal.madagascar.angonoka.dao;

import com.keepreal.madagascar.angonoka.model.SuperFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuperFollowRepository extends JpaRepository<SuperFollow, String> {
    SuperFollow findTopByPlatformIdAndTypeAndState(String platformId, int type, int state);

    SuperFollow findTopByCodeAndState(String code, int state);

    List<SuperFollow> findAllByHostIdAndState(String hostId, int state);

    SuperFollow findTopByIdAndState(String id, int state);

    @Query(value = "SELECT DISTINCT code FROM super_follow WHERE `state` = ?1 ORDER BY created_time DESC LIMIT 1", nativeQuery = true)
    String selectTopCodeByStateOrderByCreatedTime(int state);

    SuperFollow findTopByHostIdAndStateAndType(String hostId, int state, int type);
}
