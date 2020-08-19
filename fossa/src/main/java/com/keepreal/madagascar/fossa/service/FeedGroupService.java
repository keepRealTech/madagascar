package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.common.FeedGroupMessage;
import com.keepreal.madagascar.fossa.model.FeedGroup;
import com.keepreal.madagascar.fossa.dao.FeedGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the feed group service.
 */
@Service
public class FeedGroupService {

    private final FeedGroupRepository feedGroupRepository;
    private final LongIdGenerator idGenerator;

    public FeedGroupService(FeedGroupRepository feedGroupRepository,
                            LongIdGenerator idGenerator) {
        this.feedGroupRepository = feedGroupRepository;
        this.idGenerator = idGenerator;
    }

    public FeedGroupMessage getFeedGroupMessage(FeedGroup feedGroup) {
        return null;
    }

    /**
     * Inserts a new feed group entity.
     *
     * @param feedGroup {@link FeedGroup}.
     * @return {@link FeedGroup}.
     */
    public FeedGroup insert(FeedGroup feedGroup) {
        feedGroup.setId(String.valueOf(this.idGenerator.nextId()));
        feedGroup.setCreatedTime(System.currentTimeMillis());
        feedGroup.setLastFeedTime(feedGroup.getCreatedTime());
        return this.feedGroupRepository.insert(feedGroup);
    }

    /**
     * Retrieves by id.
     * @param id Feed group id.
     * @return {@link FeedGroup}.
     */
    public FeedGroup retrieveFeedGroupById(String id) {
        return this.feedGroupRepository.findByIdAndDeletedIsFalse(id);
    }

    /**
     * Updates a feed group entity.
     *
     * @param feedGroup {@link FeedGroup}.
     * @return {@link FeedGroup}.
     */
    public FeedGroup updateFeedGroup(FeedGroup feedGroup) {
        return this.feedGroupRepository.save(feedGroup);
    }

    /**
     * Deletes a feed group by id.
     *
     * @param id Feed group id.
     */
    public void deleteById(String id) {
        FeedGroup feedGroup = this.feedGroupRepository.findById(id).orElse(null);

        if (Objects.isNull(feedGroup)) {
            return;
        }

        feedGroup.setDeleted(true);
        this.feedGroupRepository.save(feedGroup);
    }

    /**
     * Retrieves all feed groups by island id.
     *
     * @param islandId Island id.
     * @param pageable {@link Pageable}.
     * @return {@link FeedGroup}.
     */
    public Page<FeedGroup> retrieveFeedGroupsByIslandId(String islandId, Pageable pageable) {
        return this.feedGroupRepository.findAllByIslandIdAndDeletedIsFalse(islandId, pageable);
    }

}
