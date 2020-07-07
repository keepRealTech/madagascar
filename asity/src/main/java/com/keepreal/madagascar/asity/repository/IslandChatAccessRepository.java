package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.IslandChatAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the island chat access repository.
 */
@Repository
public interface IslandChatAccessRepository extends JpaRepository<IslandChatAccess, String> {

    IslandChatAccess findByIslandIdAndUserIdAndDeletedIsFalse(String islandId, String userId);

}
