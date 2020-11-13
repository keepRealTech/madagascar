package com.keepreal.madagascar.angonoka.dao;

import com.keepreal.madagascar.angonoka.model.SuperFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuperFollowRepository extends JpaRepository<SuperFollow, String> {
    SuperFollow findTopByPlatformIdAndTypeAndState(String platformId, int type, int state);
}
