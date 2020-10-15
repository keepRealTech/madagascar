package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.SupportTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTargetRepository extends JpaRepository<SupportTarget, String> {

    SupportTarget findTopByIdAndDeletedIsFalse(String id);

    List<SupportTarget> findAllByIslandIdAndHostIdAndDeletedIsFalse(String islandId, String hostId);

    List<SupportTarget> findAllByHostIdAndDeletedIsFalse(String hostId);

}
