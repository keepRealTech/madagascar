package com.keepreal.madagascar.workflow.support_activity.repository.coua;

import com.keepreal.madagascar.workflow.support_activity.model.coua.IslandInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IslandRepository extends JpaRepository<IslandInfo, String> {

    @Query(value = "SELECT created_time FROM island WHERE host_id = ?1 LIMIT 1", nativeQuery = true)
    Long findIslandCreatedTimeByHostId(String userId);

    @Query(value = "SELECT host_id FROM island WHERE is_deleted IS FALSE", nativeQuery = true)
    List<String> findHostIds();
}
