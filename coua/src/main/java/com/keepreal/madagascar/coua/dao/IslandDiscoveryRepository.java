package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.IslandDiscovery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the island discovery repository.
 */
@Repository
public interface IslandDiscoveryRepository extends JpaRepository<IslandDiscovery, String> {

    List<IslandDiscovery> findAllByAuditModeAndDeletedIsFalseOrderByRankAsc(Boolean isAudit);

}
