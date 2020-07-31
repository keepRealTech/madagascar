package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.IslandIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the island identity repository.
 */
@Repository
public interface IslandIdentityRepository extends JpaRepository<IslandIdentity, String> {

    List<IslandIdentity> findAllByActiveIsTrueAndDeletedIsFalse();

}
