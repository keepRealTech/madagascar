package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.SpecialArtist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the special artists repository.
 */
@Repository
public interface SpecialArtistsTempRepository extends JpaRepository<SpecialArtist, String> {
    SpecialArtist findTopByIdAndDeletedIsFalse(String userId);
}
