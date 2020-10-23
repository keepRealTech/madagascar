package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.common.FeedGroupMessage;
import com.keepreal.madagascar.fossa.dao.FeedInfoRepository;
import com.keepreal.madagascar.fossa.model.FeedGroup;
import com.keepreal.madagascar.fossa.dao.FeedGroupRepository;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.model.PictureInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the feed group service.
 */
@Service
public class FeedGroupService {

    private final FeedInfoRepository feedInfoRepository;
    private final FeedGroupRepository feedGroupRepository;
    private final LongIdGenerator idGenerator;

    public FeedGroupService(FeedInfoRepository feedInfoRepository,
                            FeedGroupRepository feedGroupRepository,
                            LongIdGenerator idGenerator) {
        this.feedInfoRepository = feedInfoRepository;
        this.feedGroupRepository = feedGroupRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Converts the {@link FeedGroup} into {@link FeedGroupMessage}.
     *
     * @param feedGroup {@link FeedGroup}.
     * @return {@link FeedGroupMessage}.
     */
    public FeedGroupMessage getFeedGroupMessage(FeedGroup feedGroup) {
        if (Objects.isNull(feedGroup)) {
            return FeedGroupMessage.getDefaultInstance();
        }

        int feedSize = feedGroup.getImageFeedIds().size();
        List<String> uris = feedGroup.getImageFeedIds().stream()
                .skip(feedSize > 0 ? feedSize - 1 : 0)
                .map(feedId -> this.feedInfoRepository.findById(feedId).orElse(null))
                .filter(Objects::nonNull)
                .map(FeedInfo::getMediaInfos)
                .flatMap(List::stream)
                .limit(3)
                .map(mediaInfo -> ((PictureInfo) mediaInfo).getUrl())
                .collect(Collectors.toList());

        return FeedGroupMessage.newBuilder()
                .setId(feedGroup.getId())
                .setDescription(feedGroup.getDescription())
                .setIslandId(feedGroup.getIslandId())
                .setUserId(feedGroup.getHostId())
                .setName(feedGroup.getName())
                .setLastFeedTime(feedGroup.getLastFeedTime())
                .setItemsCount(feedGroup.getFeedIds().size())
                .setThumbnailUri(feedGroup.getThumbnailUri())
                .addAllImageUris(uris)
                .build();
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
     * Retrieves by ids.
     * @param ids Feed group ids.
     * @return {@link FeedGroup}.
     */
    public List<FeedGroup> retrieveFeedGroupsByIds(Iterable<String> ids) {
        return this.feedGroupRepository.findAllByIdInAndDeletedIsFalse(ids);
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
        return this.feedGroupRepository.findAllByIslandIdAndDeletedIsFalseOrderByLastFeedTimeDesc(islandId, pageable);
    }

    /**
     * Exists by user id.
     *
     * @param userId User id.
     * @return True if exists.
     */
    public Boolean existsByHostId(String userId) {
        return this.feedGroupRepository.existsByHostIdAndDeletedIsFalse(userId);
    }

}
