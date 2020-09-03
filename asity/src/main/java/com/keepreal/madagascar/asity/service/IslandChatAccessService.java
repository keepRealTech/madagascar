package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.model.IslandChatAccess;
import com.keepreal.madagascar.asity.repository.IslandChatAccessRepository;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the islance chat access service.
 */
@Service
public class IslandChatAccessService {

    private final IslandChatAccessRepository islandChatAccessRepository;
    private final LongIdGenerator idGenerator;
    private final ChatgroupService chatgroupService;

    /**
     * Constructs the island chat access service.
     *
     * @param islandChatAccessRepository {@link IslandChatAccessRepository}.
     * @param idGenerator                {@link LongIdGenerator}.
     * @param chatgroupService           {@link ChatgroupService}.
     */
    public IslandChatAccessService(IslandChatAccessRepository islandChatAccessRepository,
                                   LongIdGenerator idGenerator,
                                   ChatgroupService chatgroupService) {
        this.islandChatAccessRepository = islandChatAccessRepository;
        this.idGenerator = idGenerator;
        this.chatgroupService = chatgroupService;
    }

    /**
     * Retrieves or creates if not exists for given island and user.
     *
     * @param islandId Island id.
     * @return {@link IslandChatAccess}.
     */
    public IslandChatAccess retrieveOrCreateIslandChatAccessIfNotExistsByIslandId(String islandId) {
        IslandChatAccess islandChatAccess = this.islandChatAccessRepository.findByIslandIdAndDeletedIsFalse(islandId);
        if (Objects.nonNull(islandChatAccess)) {
            return islandChatAccess;
        }

        try {
            return this.createIslandChatAccess(islandId);
        } catch (DuplicateKeyException exception) {
            return this.islandChatAccessRepository.findByIslandIdAndDeletedIsFalse(islandId);
        }
    }

    /**
     * Enables chat access for the given island and user.
     *
     * @param islandId Island id.
     * @return {@link IslandChatAccess}.
     */
    public IslandChatAccess enable(String islandId) {
        IslandChatAccess islandChatAccess = this.retrieveOrCreateIslandChatAccessIfNotExistsByIslandId(islandId);
        islandChatAccess.setEnabled(true);
        return this.islandChatAccessRepository.save(islandChatAccess);
    }

    /**
     * Counts the enabled member count.
     *
     * @param islandId Island id.
     * @return Count.
     */
    public Integer countEnabledMember(String islandId) {
        return Math.toIntExact(this.chatgroupService.countChatgroupMembersByIslandId(islandId));
    }

    /**
     * Retrieves last enabled users.
     *
     * @param islandId Island id.
     * @return User ids.
     */
    public List<String> retrieveLastEnabledUserIds(String islandId) {
        return this.chatgroupService.retrieveLastChatgroupMemberUserIdsByIslandId(islandId);
    }

    /**
     * Creates a new entity for given island and user.
     *
     * @param islandId Island id.
     * @return {@link IslandChatAccess}.
     */
    private IslandChatAccess createIslandChatAccess(String islandId) {
        IslandChatAccess islandChatAccess = IslandChatAccess.builder()
                .id(String.valueOf(idGenerator.nextId()))
                .islandId(islandId)
                .build();

        return this.islandChatAccessRepository.save(islandChatAccess);
    }

}
