package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.model.IslandChatAccess;
import com.keepreal.madagascar.asity.repository.IslandChatAccessRepository;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the islance chat access service.
 */
@Service
public class IslandChatAccessService {

    private final IslandChatAccessRepository islandChatAccessRepository;
    private final LongIdGenerator idGenerator;

    /**
     * Constructs the island chat access service.
     *
     * @param islandChatAccessRepository    {@link IslandChatAccessRepository}.
     * @param idGenerator                   {@link LongIdGenerator}.
     */
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

    /**
     * Counts the enabled member count.
     *
     * @param islandId  Island id.
     * @return Count.
     */
    public Integer countEnabledMember(String islandId) {
        return Math.toIntExact(this.islandChatAccessRepository.countByIslandIdAndEnabledIsTrueAndDeletedIsFalse(islandId));
    }

    /**
     * Retrieves last enabled users.
     *
     * @param islandId Island id.
     * @return User ids.
     */
    public List<String> retrieveLastEnabledUserIds(String islandId) {
        return this.islandChatAccessRepository.findTop4ByIslandIdAndEnabledIsTrueAndDeletedIsFalseOrderByCreatedTimeDesc(islandId).stream()
                .map(IslandChatAccess::getUserId)
                .collect(Collectors.toList());
    }

}
