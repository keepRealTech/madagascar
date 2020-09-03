package com.keepreal.madagascar.workflow.statistics.service;

import com.keepreal.madagascar.workflow.statistics.model.IslandInfo;
import com.keepreal.madagascar.workflow.statistics.repository.IslandInfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Represents the island service.
 */
@Service
public class IslandService {

    private final IslandInfoRepository islandInfoRepository;

    /**
     * Constructs the island info service.
     *
     * @param islandInfoRepository {@link IslandInfoRepository}.
     */
    public IslandService(IslandInfoRepository islandInfoRepository) {
        this.islandInfoRepository = islandInfoRepository;
    }

    /**
     * Retrieves islands by ids.
     *
     * @param ids Island ids.
     * @return {@link IslandInfo}.
     */
    public List<IslandInfo> retrieveIslandsByIds(Iterable<String> ids) {
        return this.islandInfoRepository.findByIdInAndDeletedIsFalse(ids);
    }

}
