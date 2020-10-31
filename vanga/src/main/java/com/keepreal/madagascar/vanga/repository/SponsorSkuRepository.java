package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.SponsorSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the sponsor sku repository.
 */
@Repository
public interface SponsorSkuRepository extends JpaRepository<SponsorSku, String> {

    List<SponsorSku> findAllBySponsorIdAndActiveIsTrueAndDeletedIsFalse(String sponsorId);

    List<SponsorSku> findAllByIslandIdAndActiveIsTrueAndDeletedIsFalse(String islandId);

    List<SponsorSku> findAllBySponsorIdAndDeletedIsFalse(String sponsorId);

    SponsorSku findTopByIdAndDeletedIsFalse(String id);

}
