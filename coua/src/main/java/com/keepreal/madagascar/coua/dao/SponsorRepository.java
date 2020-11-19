package com.keepreal.madagascar.coua.dao;


import com.keepreal.madagascar.coua.model.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the sponsor repository.
 */
@Repository
public interface SponsorRepository extends JpaRepository<Sponsor, String> {
    Sponsor findTopByIslandIdAndActiveIsTrueAndDeletedIsFalse(String islandId);

    Sponsor findTopByHostIdAndActiveIsTrueAndDeletedIsFalse(String hostId);
}
