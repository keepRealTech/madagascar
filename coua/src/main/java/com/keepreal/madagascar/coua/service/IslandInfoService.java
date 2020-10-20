package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.IslandAccessType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.coua.DiscoverIslandMessage;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.SupportTargetMessage;
import com.keepreal.madagascar.coua.TargetType;
import com.keepreal.madagascar.coua.TimeType;
import com.keepreal.madagascar.coua.dao.IslandDiscoveryRepository;
import com.keepreal.madagascar.coua.dao.IslandInfoRepository;
import com.keepreal.madagascar.coua.dao.SupportTargetRepository;
import com.keepreal.madagascar.coua.dao.UserInfoRepository;
import com.keepreal.madagascar.coua.model.IslandDiscovery;
import com.keepreal.madagascar.coua.model.IslandInfo;
import com.keepreal.madagascar.coua.model.SupportTarget;
import com.keepreal.madagascar.coua.util.PageResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.Comparator;
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
    private final UserInfoRepository userInfoRepository;
    private final SupportTargetRepository supportTargetRepository;

    /**
     * Constructs the island service.
     *
     * @param islandDiscoveryRepository {@link IslandDiscoveryRepository}.
     * @param islandInfoRepository      {@link IslandInfoRepository}.
     * @param subscriptionService       {@link SubscriptionService}.
     * @param idGenerator               {@link LongIdGenerator}.
     * @param userInfoRepository        {@link UserInfoRepository}.
     * @param supportTargetRepository   {@link SupportTargetRepository}
     */
    public IslandInfoService(IslandDiscoveryRepository islandDiscoveryRepository,
                             IslandInfoRepository islandInfoRepository,
                             SubscriptionService subscriptionService,
                             LongIdGenerator idGenerator,
                             UserInfoRepository userInfoRepository,
                             SupportTargetRepository supportTargetRepository) {
        this.islandDiscoveryRepository = islandDiscoveryRepository;
        this.islandInfoRepository = islandInfoRepository;
        this.subscriptionService = subscriptionService;
        this.idGenerator = idGenerator;
        this.userInfoRepository = userInfoRepository;
        this.supportTargetRepository = supportTargetRepository;
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
                .setShowIncome(islandInfo.getShowIncome())
                .setCustomUrl(islandInfo.getCustomUrl())
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
     * 根据用户名或者岛名前缀查询，查询结果根据该用户是否加V排序，如果同加V，比较岛民人数
     *
     * @param name islandName or username.
     * @param pageable   {@link Pageable}.
     * @param builder    {@link com.keepreal.madagascar.coua.IslandResponse.Builder}.
     * @return {@link IslandInfo}.
     */
    public List<IslandInfo> getIslandByName(String name, Pageable pageable, IslandsResponse.Builder builder) {
        Page<String> userIdListPageable = this.userInfoRepository.findUserIdByNameOrderByIdentity(name, pageable);
        List<IslandInfo> islandInfosWithV = this.islandInfoRepository.findIslandInfosByHostIdIn(userIdListPageable.getContent());
        List<IslandInfo> sortedIslandWithV = islandInfosWithV.stream().sorted(Comparator.comparing(IslandInfo::getIslanderNumber).reversed()).collect(Collectors.toList());

        Page<IslandInfo> islandListPageable = islandInfoRepository.findByIslandNameStartingWithAndDeletedIsFalse(name, pageable);
        List<IslandInfo> sortedIsland = islandListPageable.stream().sorted(Comparator.comparing(IslandInfo::getIslanderNumber).reversed()).collect(Collectors.toList());

        if (islandListPageable.hasContent()) {
            builder.setPageResponse(PageResponseUtil.buildResponse(islandListPageable));
        } else {
            builder.setPageResponse(PageResponseUtil.buildResponse(userIdListPageable));
        }

        for (IslandInfo islandInfo : sortedIsland) {
            if (!sortedIslandWithV.contains(islandInfo)) {
                sortedIslandWithV.add(islandInfo);
            }
        }

        return sortedIslandWithV;
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
        List<IslandDiscovery> islandDiscoveryList = this.islandDiscoveryRepository.findAllByDeletedIsFalseOrderByRankAsc();

        Map<String, IslandDiscovery> islandDiscoveryMap = islandDiscoveryList.stream()
                .collect(Collectors.toMap(IslandDiscovery::getIslandId, Function.identity(), (mem1, mem2) -> mem1, HashMap::new));

        List<IslandInfo> islandInfoList = this.retrieveByIslandIds(islandDiscoveryList.stream()
                .map(IslandDiscovery::getIslandId)
                .collect(Collectors.toList()));

        Map<String, IslandInfo> islandInfoMap = islandInfoList.stream()
                .collect(Collectors.toMap(IslandInfo::getId, Function.identity(), (mem1, mem2) -> mem1, HashMap::new));

        return islandDiscoveryList.stream()
                .map(islandDiscovery ->
                     DiscoverIslandMessage.newBuilder()
                            .setIsland(this.getIslandMessage(islandInfoMap.get(islandDiscovery.getIslandId())))
                            .setRecommendation(islandDiscoveryMap.get(islandDiscovery.getIslandId()).getRecommendation())
                            .build())
                .collect(Collectors.toList());
    }

    /**
     * 根据 id 查询支持目标
     *
     * @param id 支持目标主键id
     * @return {@link SupportTarget}
     */
    public SupportTarget findSupportTargetByIdAndDeletedIsFalse(String id) {
        return this.supportTargetRepository.findTopByIdAndDeletedIsFalse(id);
    }

    /**
     * 更新支持目标
     *
     * @param supportTarget {@link SupportTarget}
     * @return {@link SupportTarget}
     */
    public SupportTarget updateSupportTarget(SupportTarget supportTarget) {
        return this.supportTargetRepository.save(supportTarget);
    }

    /**
     * 创建支持目标
     *
     * @param supportTarget {@link SupportTarget}
     * @return {@link SupportTarget}
     */
    public SupportTarget createSupportTarget(SupportTarget supportTarget) {
        supportTarget.setId(String.valueOf(idGenerator.nextId()));
        return this.updateSupportTarget(supportTarget);
    }

    /**
     * converts {@link SupportTarget} to the {@link SupportTargetMessage}
     *
     * @param supportTarget {@link SupportTarget}
     * @return {@link SupportTargetMessage}
     */
    public SupportTargetMessage getSupportTargetMessage(SupportTarget supportTarget) {
        if (Objects.isNull(supportTarget)) {
            return null;
        }
        return SupportTargetMessage.newBuilder()
                .setId(supportTarget.getId())
                .setContent(supportTarget.getContent())
                .setHostId(supportTarget.getHostId())
                .setIslandId(supportTarget.getIslandId())
                .setTargetTypeValue(supportTarget.getTargetType())
                .setTimeTypeValue(supportTarget.getTimeType())
                .setCurrentSupporterNum(supportTarget.getCurrentSupporterNum())
                .setTotalSupporterNum(supportTarget.getTotalSupporterNum())
                .setCurrentAmountInCents(supportTarget.getCurrentAmountInCents())
                .setTotalAmountInCents(supportTarget.getTotalAmountInCents())
                .build();
    }

    /**
     * 根据 岛id 获取支持目标
     *
     * @param islandId 岛id
     * @return {@link List<SupportTarget>}
     */
    public List<SupportTarget> findAllSupportTargetByIslandId(String islandId) {
        return this.supportTargetRepository.findAllByIslandIdAndDeletedIsFalse(islandId);
    }

    /**
     * 如果有支持目标就根据类型更新支持目标
     *
     * @param hostId 岛主id
     * @param amountInCents 金额
     */
    public void updateSupportTargetIfExisted(String hostId, Long amountInCents) {
        List<SupportTarget> supportTargets = this.supportTargetRepository.findAllByHostIdAndDeletedIsFalse(hostId);
        if (!CollectionUtils.isEmpty(supportTargets)) {
            SupportTarget supportTarget = supportTargets.get(0);
            switch (this.convertToTargetType(supportTarget.getTargetType())) {
                case SUPPORTER:
                    supportTarget.setCurrentSupporterNum(supportTarget.getCurrentSupporterNum() + 1L);
                    break;
                case AMOUNT:
                    supportTarget.setCurrentAmountInCents(supportTarget.getCurrentAmountInCents() + amountInCents);
                    break;
                case UNRECOGNIZED:
                    break;
            }
            this.updateSupportTarget(supportTarget);
        }
    }

    /**
     * converts int to {@link TargetType}
     *
     * @param targetTypeInteger int num
     * @return {@link TargetType}
     */
    public TargetType convertToTargetType(Integer targetTypeInteger) {
        switch (targetTypeInteger) {
            case 1:
                return TargetType.SUPPORTER;
            case 2:
                return TargetType.AMOUNT;
            default:
                return TargetType.UNRECOGNIZED;
        }
    }

    /**
     * 目标数据每月1日0:00归零
     */
    @Scheduled(cron = "0 0 0 1 * ? ")
    public void clearSupportTargetPerMonth() {
        this.supportTargetRepository.clearSupportTargetByTimeType(TimeType.PER_MONTH_VALUE);
    }

    /**
     * 检查自定义首页链接是否已存在
     *
     * @param customUrl 自定义链接
     * @return 存在返回true
     */
    public boolean checkIslandCustomUrl(String customUrl) {
        IslandInfo islandInfo = this.islandInfoRepository.findTopByCustomUrlAndDeletedIsFalse(customUrl);
        return Objects.nonNull(islandInfo);
    }

}