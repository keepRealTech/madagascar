package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.UserMessage;
import org.springframework.stereotype.Service;
import swagger.model.BriefIslandDTO;
import swagger.model.IslandDTO;
import swagger.model.IslandProfileDTO;

import java.util.Objects;

/**
 * Represents the island dto factory.
 */
@Service
public class IslandDTOFactory {

    private final UserDTOFactory userDTOFactory;

    /**
     * Constructs the island dto factory.
     *
     * @param userDTOFactory {@link UserDTOFactory}.
     */
    public IslandDTOFactory(UserDTOFactory userDTOFactory) {
        this.userDTOFactory = userDTOFactory;
    }

    /**
     * Converts {@link IslandMessage} to {@link IslandDTO}.
     *
     * @param island {@link IslandMessage}.
     * @return {@link IslandDTO}.
     */
    public IslandDTO valueOf(IslandMessage island) {
        if (Objects.isNull(island)) {
            return null;
        }

        IslandDTO islandDTO = new IslandDTO();
        islandDTO.setId(island.getId());
        islandDTO.setName(island.getName());
        islandDTO.setMemberCount(island.getMemberCount());
        islandDTO.setDescription(island.getDescription());
        islandDTO.setHostId(island.getHostId());
        islandDTO.setPortraitImageUri(island.getPortraitImageUri());

        return islandDTO;
    }

    /**
     * Converts {@link IslandMessage} to {@link BriefIslandDTO}.
     *
     * @param island {@link IslandMessage}.
     * @return {@link BriefIslandDTO}.
     */
    public BriefIslandDTO briefValueOf(IslandMessage island) {
        if (Objects.isNull(island)) {
            return null;
        }

        BriefIslandDTO briefIslandDTO = new BriefIslandDTO();
        briefIslandDTO.setId(island.getId());
        briefIslandDTO.setName(island.getName());
        briefIslandDTO.setDescription(island.getDescription());
        briefIslandDTO.setHostId(island.getHostId());
        briefIslandDTO.setPortraitImageUri(island.getPortraitImageUri());

        return briefIslandDTO;
    }

    /**
     * Converts {@link IslandMessage} to {@link IslandDTO}.
     *
     * @param island {@link IslandMessage}.
     * @param host   Host {@link UserMessage}.
     * @return {@link IslandProfileDTO}.
     */
    public IslandProfileDTO valueOf(IslandMessage island, UserMessage host) {
        IslandProfileDTO islandProfileDTO = new IslandProfileDTO();
        islandProfileDTO.setIsland(this.valueOf(island));
        islandProfileDTO.setHost(this.userDTOFactory.valueOf(host));

        return islandProfileDTO;
    }

}
