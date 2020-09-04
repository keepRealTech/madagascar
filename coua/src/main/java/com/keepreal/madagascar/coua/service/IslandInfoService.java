package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.IslandAccessType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.coua.DiscoverIslandMessage;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.dao.IslandDiscoveryRepository;
import com.keepreal.madagascar.coua.dao.IslandInfoRepository;
import com.keepreal.madagascar.coua.model.IslandDiscovery;
import com.keepreal.madagascar.coua.model.IslandInfo;
import com.keepreal.madagascar.coua.util.PageResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents island service.
 */
@Slf4j
@Service
public class IslandInfoService {

    private final IslandDiscoveryRepository islandDiscoveryRepository;
    private final IslandInfoRepository islandInfoRepository;
    private final SubscriptionService subscriptionService;
    private final LongIdGenerator idGenerator;

    /**
     * Constructs the island service.
     *
     * @param islandDiscoveryRepository {@link IslandDiscoveryRepository}.
     * @param islandInfoRepository      {@link IslandInfoRepository}.
     * @param subscriptionService       {@link SubscriptionService}.
     * @param idGenerator               {@link LongIdGenerator}.
     */
    public IslandInfoService(IslandDiscoveryRepository islandDiscoveryRepository,
                             IslandInfoRepository islandInfoRepository,
                             SubscriptionService subscriptionService,
                             LongIdGenerator idGenerator) {
        this.islandDiscoveryRepository = islandDiscoveryRepository;
        this.islandInfoRepository = islandInfoRepository;
        this.subscriptionService = subscriptionService;
        this.idGenerator = idGenerator;
    }

    /**
     * Retrieve latest island number.
     *
     * @param islandId islandId.
     * @return latest number.
     */
    public Integer getLatestIslanderNumber(String islandId) {
        return islandInfoRepository.getIslanderNumberByIslandId(islandId);
    }

    /**
     * if islandName is existed.
     *
     * @param islandName islandName.
     * @return is existed.
     */
    public boolean islandNameIsExisted(String islandName) {
        return islandInfoRepository.findTopByIslandNameAndDeletedIsFalse(islandName) != null;
    }

    /**
     * Retrieve islandMessage.
     *
     * @param islandInfo {@link IslandInfo}.
     * @return {@link IslandMessage}.
     */
    public IslandMessage getIslandMessage(IslandInfo islandInfo) {
        if (islandInfo == null) {
            return null;
        }
        Integer memberCount = subscriptionService.getMemberCountByIslandId(islandInfo.getId());
        return IslandMessage
                .newBuilder()
                .setId(islandInfo.getId())
                .setName(islandInfo.getIslandName())
                .setHostId(islandInfo.getHostId())
                .setPortraitImageUri(islandInfo.getPortraitImageUri())
                .setDescription(islandInfo.getDescription())
                .setLastFeedAt(islandInfo.getLastFeedAt())
                .setCreatedAt(islandInfo.getCreatedTime())
                .setSecret(islandInfo.getSecret())
                .setMemberCount(memberCount)
                .setIslandAccessType(IslandAccessType.forNumber(islandInfo.getIslandAccessType()))
                .build();
    }

    /**
     * build the feed message.
     *
     * @param islandId         islandId.
     * @param islandLastFeedAt islandLastFeedAt.
     * @param currentTime      currentTime.
     * @return {@link CheckNewFeedsMessage}.
     */
    public CheckNewFeedsMessage buildFeedMessage(String islandId, Long islandLastFeedAt, Long currentTime) {
        return CheckNewFeedsMessage.newBuilder()
                .setIslandId(islandId)
                .setHasNewFeeds(islandLastFeedAt != null && islandLastFeedAt > currentTime)
                .build();
    }

    /**
     * Retrieves islandList by user create.
     *
     * @param userId   userId.
     * @param pageable {@link Pageable}.
     * @param builder  {@link com.keepreal.madagascar.coua.IslandsResponse.Builder}.
     * @return {@link IslandInfo}.
     */
    public List<IslandInfo> getMyCreatedIsland(String userId, Pageable pageable, IslandsResponse.Builder builder) {
        Page<String> islandIdListPageable = subscriptionService.getIslandIdListByUserCreated(userId, pageable);
        builder.setPageResponse(PageResponseUtil.buildResponse(islandIdListPageable));
        if (!islandIdListPageable.hasContent()) {
            return Collections.emptyList();
        }
        return islandInfoRepository.findIslandInfosByIdInAndDeletedIsFalse(islandIdListPageable.getContent());
    }

    /**
     * Retrieves user created islands.
     *
     * @param userId   User id.
     * @param pageable {@link Pageable}.
     * @return {@link IslandInfo}.
     */
    public Page<IslandInfo> getMyCreatedIslands(String userId, Pageable pageable) {
        return this.islandInfoRepository.findAllByHostIdAndDeletedIsFalse(userId, pageable);
    }

    /**
     * Retrieve islandList by islandName.
     *
     * @param islandName islandName.
     * @param pageable   {@link Pageable}.
     * @param builder    {@link com.keepreal.madagascar.coua.IslandResponse.Builder}.
     * @return {@link IslandInfo}.
     */
    public List<IslandInfo> getIslandByName(String islandName, Pageable pageable, IslandsResponse.Builder builder) {
        Page<IslandInfo> islandListPageable = islandInfoRepository.findByIslandNameStartingWithAndDeletedIsFalse(islandName, pageable);

        Page<String> islandIdsPageable = this.subscriptionService.getIslandIdsByUsername(islandName, pageable);
        Set<String> islandIds = new HashSet<>(islandIdsPageable.getContent());
        islandIds.removeAll(islandListPageable.getContent().stream().map(IslandInfo::getId).collect(Collectors.toList()));
        List<IslandInfo> islandInfos = this.islandInfoRepository.findByIdInAndDeletedIsFalse(islandIds);
        islandInfos.addAll(islandListPageable.getContent());

        if (islandListPageable.hasContent()) {
            builder.setPageResponse(PageResponseUtil.buildResponse(islandListPageable));
        } else {
            builder.setPageResponse(PageResponseUtil.buildResponse(islandIdsPageable));
        }

        return islandInfos.stream().sorted(Comparator.comparing((IslandInfo island) -> island.getIdentityId().length()).thenComparing(IslandInfo::getIslanderNumber).reversed()).collect(Collectors.toList());
    }

    /**
     * Retrieve islandList by islandName and user subscribed.
     *
     * @param islandName islandName.
     * @param userId     userId.
     * @return {@link IslandInfo}.
     */
    public List<IslandInfo> getIslandByNameAndSubscribed(String islandName, String userId) {
        IslandInfo islandInfo = islandInfoRepository.findTopByIslandNameAndDeletedIsFalse(islandName);
        if (islandInfo == null || !subscriptionService.isSubScribedIsland(islandInfo.getId(), userId)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(islandInfo);
    }

    /**
     * Retrieve islandList by user created and subscribed.
     *
     * @param userId   userId.
     * @param pageable {@link Pageable}.
     * @param builder  {@link com.keepreal.madagascar.coua.IslandsResponse.Builder}.
     * @return {@link IslandInfo}.
     */
    public List<IslandInfo> getIslandBySubscribed(String userId, Pageable pageable, IslandsResponse.Builder builder) {
        Page<String> islandIdListPageable = subscriptionService.getIslandIdListByUserSubscribed(userId, pageable);
        builder.setPageResponse(PageResponseUtil.buildResponse(islandIdListPageable));
        if (!islandIdListPageable.hasContent()) {
            return Collections.emptyList();
        }
        return islandInfoRepository.findIslandInfosByIdInAndDeletedIsFalse(islandIdListPageable.getContent());
    }

    /**
     * Retrieve all island.
     *
     * @param pageable {@link Pageable}.
     * @param builder  {@link com.keepreal.madagascar.coua.IslandsResponse.Builder}.
     * @return {@link IslandInfo}.
     */
    public List<IslandInfo> getIsland(Pageable pageable, IslandsResponse.Builder builder) {
        Page<IslandInfo> islandInfoListPageable = islandInfoRepository.findAllByDeletedIsFalse(pageable);
        builder.setPageResponse(PageResponseUtil.buildResponse(islandInfoListPageable));
        return islandInfoListPageable.getContent();
    }

    /**
     * Create island.
     *
     * @param islandInfo {@link IslandInfo}.
     * @return {@link IslandInfo}.
     */
    public IslandInfo createIsland(IslandInfo islandInfo) {
        islandInfo.setId(String.valueOf(idGenerator.nextId()));
        IslandInfo save = this.updateIsland(islandInfo);
        // 维护 subscription 表
        subscriptionService.initHost(save.getId(), save.getHostId());
        return save;
    }

    /**
     * Retrieve island by id and not delete.
     *
     * @param islandId islandId.
     * @return {@link IslandInfo}.
     */
    public IslandInfo findTopByIdAndDeletedIsFalse(String islandId) {
        return islandInfoRepository.findTopByIdAndDeletedIsFalse(islandId);
    }

    /**
     * Update island.
     *
     * @param islandInfo {@link IslandInfo}.
     * @return {@link IslandInfo}.
     */
    public IslandInfo updateIsland(IslandInfo islandInfo) {
        try {
            return islandInfoRepository.save(islandInfo);
        } catch (Exception e) {
            log.error("[updateIsland] island sql duplicate error! island id is [{}], island name is [{}]", islandInfo.getId(), islandInfo.getIslandName());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_ISLAND_SQL_DUPLICATE_ERROR);
        }
    }

    /**
     * Retrieve map by islandList. (key-islandId, value-lastFeedAt)
     *
     * @param islandIdList islandIdList.
     * @return (key - islandId, value - lastFeedAt).
     */
    public List<Map<String, Long>> findIslandIdAndLastFeedAtByIslandIdList(List<String> islandIdList) {
        return islandInfoRepository.findIslandIdAndLastFeedAtByIslandIdList(islandIdList);
    }

    /**
     * Update lastFeedAt by islandIdList.
     *
     * @param islandIdList islandIdList.
     * @param timestamps   timestamps.
     */
    public void updateLastFeedAtByIslandIdList(List<String> islandIdList, long timestamps) {
        islandInfoRepository.updateLastFeedAtByIslandIdList(islandIdList, timestamps);
    }

    /**
     * Retrieves all by ids.
     *
     * @param ids Island ids.
     * @return {@link IslandInfo}.
     */
    public List<IslandInfo> retrieveByIslandIds(Iterable<String> ids) {
        return this.islandInfoRepository.findAllByIdInAndDeletedIsFalse(ids);
    }

    /**
     * Retrieves all discovered islands.
     *
     * @return {@link DiscoverIslandMessage}.
     */
    public List<DiscoverIslandMessage> retrieveAllDiscoveredIslands() {
        List<IslandDiscovery> islandDiscoveryList = this.islandDiscoveryRepository.findAllByDeletedIsFalseOrderByCreatedTimeDesc();

        Map<String, IslandDiscovery> islandDiscoveryMap = islandDiscoveryList
                        .stream().collect(Collectors.toMap(IslandDiscovery::getIslandId, Function.identity(), (mem1, mem2) -> mem1, HashMap::new));

        List<IslandInfo> islandInfoList = this.retrieveByIslandIds(islandDiscoveryList.stream().map(IslandDiscovery::getIslandId).collect(Collectors.toList()));

        Map<String, IslandInfo> islandInfoMap = islandInfoList
                .stream().collect(Collectors.toMap(IslandInfo::getId, Function.identity(), (mem1, mem2) -> mem1, HashMap::new));

        return islandDiscoveryList.stream()
                .map(islandDiscovery ->
                     DiscoverIslandMessage.newBuilder()
                            .setIsland(this.getIslandMessage(islandInfoMap.get(islandDiscovery.getIslandId())))
                            .setRecommendation(islandDiscoveryMap.get(islandDiscovery.getIslandId()).getRecommendation())
                            .build()
                ).collect(Collectors.toList());
    }

}