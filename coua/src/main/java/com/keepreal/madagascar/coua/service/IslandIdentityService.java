package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.coua.IslandIdentityMessage;
import com.keepreal.madagascar.coua.dao.IslandIdentityRepository;
import com.keepreal.madagascar.coua.model.IslandIdentity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the island identity service.
 */
@Service
public class IslandIdentityService {

    private final IslandIdentityRepository islandIdentityRepository;

    /**
     * Constructs the island identity service.
     *
     * @param islandIdentityRepository {@link IslandIdentityRepository}.
     */
    public IslandIdentityService(IslandIdentityRepository islandIdentityRepository) {
        this.islandIdentityRepository = islandIdentityRepository;
    }

    /**
     * Constructs the active island identities.
     *
     * @return {@link IslandIdentity}.
     */
    public List<IslandIdentity> retrieveActiveIslandIdentities() {
        return this.islandIdentityRepository.findAllByActiveIsTrueAndDeletedIsFalse();
    }

    /**
     * Converts {@link IslandIdentity} into {@link IslandIdentityMessage}.
     *
     * @param islandIdentity {@link IslandIdentity}.
     * @return {@link IslandIdentityMessage}.
     */
    public IslandIdentityMessage getIslandIdentityMessage(IslandIdentity islandIdentity) {
        if (Objects.isNull(islandIdentity)) {
            return null;
        }

        return IslandIdentityMessage.newBuilder()
                .setId(islandIdentity.getId())
                .setDescription(islandIdentity.getDescription())
                .setIconUri(islandIdentity.getIconImageUri())
                .setName(islandIdentity.getName())
                .setStartColor(islandIdentity.getStartColor())
                .setEndColor(islandIdentity.getEndColor())
                .build();
    }

}
