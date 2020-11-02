package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.SponsorHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


/**
 * Represents the sponsor history repository.
 */
@Repository
public interface SponsorHistoryRepository extends JpaRepository<SponsorHistory, String> {

    Page<SponsorHistory> findAllByIslandIdAndDeletedIsFalseOrderByCreatedTimeDesc(String islandId, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM sponsor_history WHERE island_id = ?1 AND is_deleted = FALSE", nativeQuery = true)
    Long getSponsorCountByIslandId(String islandId);

}
