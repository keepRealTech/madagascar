package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.SupportTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SupportTargetRepository extends JpaRepository<SupportTarget, String> {

    SupportTarget findTopByIdAndDeletedIsFalse(String id);

    List<SupportTarget> findAllByIslandIdAndHostIdAndDeletedIsFalse(String islandId, String hostId);

    List<SupportTarget> findAllByHostIdAndDeletedIsFalse(String hostId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE support_target " +
            "SET current_supporter_num = 0 " +
            "AND current_amount_in_cents = 0 " +
            "WHERE time_type = ?1", nativeQuery = true)
    void clearSupportTargetByTimeType(Integer timeType);

}
