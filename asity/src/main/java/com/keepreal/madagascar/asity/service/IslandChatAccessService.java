package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.model.IslandChatAccess;
import com.keepreal.madagascar.asity.repository.IslandChatAccessRepository;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the islance chat access service.
 */
@Service
public class IslandChatAccessService {

    private final IslandChatAccessRepository islandChatAccessRepository;
    private final LongIdGenerator idGenerator;

    public IslandChatAccessService(IslandChatAccessRepository islandChatAccessRepository,
                                   LongIdGenerator idGenerator) {
        this.islandChatAccessRepository = islandChatAccessRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Retrieves or creates if not exists for given island and user.
     *
     * @param islandId   Island id.
     * @param userId     User id.
     * @return {@link IslandChatAccess}.
     */
    public IslandChatAccess retrieveOrCreateIslandChatAccessIfNotExistsByIslandIdAndUserId(String islandId, String userId) {
        IslandChatAccess islandChatAccess = this.islandChatAccessRepository.findByIslandIdAndUserIdAndDeletedIsFalse(islandId, userId);
        if (Objects.nonNull(islandChatAccess)) {
            return islandChatAccess;
        }

        return this.createIslandChatAccess(islandId, userId);
    }

    /**
     * Creates a new entity for given island and user.
     *
     * @param islandId  Island id.
     * @param userId    User id.
     * @return {@link IslandChatAccess}.
     */
    public IslandChatAccess createIslandChatAccess(String islandId, String userId) {
        IslandChatAccess islandChatAccess = IslandChatAccess.builder()
                .id(String.valueOf(idGenerator.nextId()))
                .islandId(islandId)
                .userId(userId)
                .build();

        return this.islandChatAccessRepository.save(islandChatAccess);
    }

    /**
     * Enables chat access for the given island and user.
     *
     * @param islandId  Island id.
     * @param userId    User id.
     * @return {@link IslandChatAccess}.
     */
    public IslandChatAccess enable(String islandId, String userId) {
        IslandChatAccess islandChatAccess = this.retrieveOrCreateIslandChatAccessIfNotExistsByIslandIdAndUserId(islandId, userId);
        islandChatAccess.setEnabled(true);
        return this.islandChatAccessRepository.save(islandChatAccess);
    }

}
