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

    SuperFollow findTopByHostIdAndStateAndType(String hostId, int state, int type);
}
