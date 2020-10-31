package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.SponsorGift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the sponsor gift repository.
 */
@Repository
public interface SponsorGiftRepository extends JpaRepository<SponsorGift, String> {
    List<SponsorGift> findAllByIsDefaultAndDeletedIsFalseOrderByCreatedTimeAsc(boolean isDefault);

    List<SponsorGift> findAllByDeletedIsFalseOrderByCreatedTimeAsc();

    SponsorGift findTopByIdAndDeletedIsFalse(String id);
}
