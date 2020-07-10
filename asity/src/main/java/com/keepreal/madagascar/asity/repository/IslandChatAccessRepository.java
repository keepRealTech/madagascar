package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.IslandChatAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the island chat access repository.
 */
@Repository
public interface IslandChatAccessRepository extends JpaRepository<IslandChatAccess, String> {

    IslandChatAccess findByIslandIdAndUserIdAndDeletedIsFalse(String islandId, String userId);

    Long countByIslandIdAndEnabledIsTrueAndDeletedIsFalse(String islandId);

    List<IslandChatAccess> findTop4ByIslandIdAndEnabledIsTrueAndDeletedIsFalseOrderByCreatedTimeDesc(String islandId);

}
