package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.FeedGroupMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.DiscoverIslandMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.lemur.dtoFactory.FeedDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.IslandDTOFactory;
import com.keepreal.madagascar.lemur.service.FeedGroupService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swagger.model.IslandDiscoveryResponse;
import swagger.model.IslandsResponse;
import swagger.model.TimelinesResponse;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the audit controller.
 */
@RestController
public class AuditController {

    private final IslandService islandService;
    private final IslandDTOFactory islandDTOFactory;
    private final FeedService feedService;
    private final FeedDTOFactory feedDTOFactory;
    private final FeedGroupService feedGroupService;
    private final MembershipService membershipService;

    /**
     * Constructs the audit controller.
     *
     * @param islandService     {@link IslandService}.
     * @param islandDTOFactory  {@link IslandDTOFactory}.
     * @param feedService       {@link FeedService}.
     * @param feedDTOFactory    {@link FeedDTOFactory}.
     * @param feedGroupService  {@link FeedGroupService}.
     * @param membershipService {@link MembershipService}.
     */
    public AuditController(IslandService islandService,
                           IslandDTOFactory islandDTOFactory,
                           FeedService feedService,
                           FeedDTOFactory feedDTOFactory,
                           FeedGroupService feedGroupService,
                           MembershipService membershipService) {
        this.islandService = islandService;
        this.islandDTOFactory = islandDTOFactory;
        this.feedService = feedService;
        this.feedDTOFactory = feedDTOFactory;
        this.feedGroupService = feedGroupService;
        this.membershipService = membershipService;
    }

    /**
     * Implements the audit island search method.
     *
     * @param name       Island name.
     * @param subscribed Whether subscribed.
     * @param page       Page index.
     * @param pageSize   Page size.
     * @return Empty list.
     */
    @RequestMapping(value = "/api/v0/islands",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<IslandsResponse> apiV0IslandsGet(@RequestParam(value = "name", required = false) String name,
                                                           @RequestParam(value = "subscribed", required = false) Boolean subscribed,
                                                           @Valid @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                           @Valid @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        IslandsResponse response = new IslandsResponse();
        response.setData(Collections.emptyList());
        response.setPageInfo(PaginationUtils.getPageInfo(false, false, pageSize));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the audit mode discover islands.
     *
     * @return {@link IslandDiscoveryResponse}.
     */
    @RequestMapping(value = "/api/v0/islands/discovery",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<IslandDiscoveryResponse> apiV0IslandsDiscoveryGet() {
        List<DiscoverIslandMessage> discoverIslandMessageList = this.islandService.retrieveIslandsInDiscovery(true);

        IslandDiscoveryResponse response = new IslandDiscoveryResponse();
        response.setData(discoverIslandMessageList.stream()
                .filter(Objects::nonNull)
                .map(this.islandDTOFactory::valueOf)
                .collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get public accessible feeds api for audit mode.
     *
     * @param minTimestamp      (optional, default to 0) minimal feed created timestamp.
     * @param maxTimestamp      (optional, default to 0) maximal feed created timestamp.
     * @param pageSize          (optional, default to 10) size of a page .
     * @param includeChargeable Whether includes the chargeable feeds.
     * @return {@link TimelinesResponse}.
     */
    @RequestMapping(value = "/api/v0/feeds/public",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<TimelinesResponse> apiV1FeedsPublicGet(@Valid @RequestParam(value = "minTimestamp", required = false) Long minTimestamp,
                                                                 @Valid @RequestParam(value = "maxTimestamp", required = false) Long maxTimestamp,
                                                                 @Valid @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                                 @Valid @RequestParam(value = "includeChargeable", required = false, defaultValue = "false") Boolean includeChargeable) {
        String userId = HttpContextUtils.getUserIdFromContext();

        List<DiscoverIslandMessage> discoverIslandMessageList = this.islandService.retrieveIslandsInDiscovery(true);

        List<FeedMessage> feeds = discoverIslandMessageList.stream()
                .map(island -> this.feedService.retrieveIslandFeeds(island.getIsland().getId(), true, userId, minTimestamp, maxTimestamp, 0, pageSize, false).getFeedList())
                .flatMap(List::stream)
                .collect(Collectors.toList());

        Map<String, List<MembershipMessage>> feedMembershipMap = this.generateFeedMembershipMap(feeds);

        Map<String, FeedGroupMessage> feedGroupMessageMap = this.generateFeedGroupMap(feeds);

        TimelinesResponse response = new TimelinesResponse();
        response.setData(feeds
                .stream()
                .map(feed -> this.feedDTOFactory.valueOf(feed,
                        feedMembershipMap.getOrDefault(feed.getId(), Collections.emptyList()),
                        false,
                        feed.getCreatedAt(),
                        feedGroupMessageMap.get(feed.getFeedgroupId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(feeds.size() > 0, false, pageSize));
        response.setCurrentTime(System.currentTimeMillis());
        response.setHasFeedgroup(Boolean.TRUE.equals(this.feedGroupService.existsFeedGroupsByUserId(userId)));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Generates the feed membership map.
     *
     * @param feeds {@link FeedMessage}.
     * @return Feed id vs {@link MembershipMessage}.
     */
    private Map<String, List<MembershipMessage>> generateFeedMembershipMap(List<FeedMessage> feeds) {
        Set<String> membershipIdSet = feeds.stream()
                .map(FeedMessage::getMembershipIdList)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        Map<String, MembershipMessage> membershipMap = new HashMap<>();
        if (!membershipIdSet.isEmpty()) {
            membershipMap = this.membershipService.retrieveMembershipsByIds(membershipIdSet).stream()
                    .collect(Collectors.toMap(MembershipMessage::getId, Function.identity(), (feed1, feed2) -> feed1, HashMap::new));
        }

        Map<String, List<MembershipMessage>> feedMembershipMap = new HashMap<>();
        if (!membershipIdSet.isEmpty()) {
            Map<String, MembershipMessage> finalMembershipMap = membershipMap;
            feedMembershipMap = feeds.stream()
                    .collect(Collectors.toMap(
                            FeedMessage::getId,
                            feed -> feed.getMembershipIdList().stream().map(finalMembershipMap::get).filter(Objects::nonNull).collect(Collectors.toList()),
                            (memberships1, memberships2) -> memberships1,
                            HashMap::new));
        }

        return feedMembershipMap;
    }

    /**
     * Generates the feed group map.
     *
     * @param feeds {@link FeedMessage}.
     * @return Feed group id vs {@link FeedGroupMessage}.
     */
    private Map<String, FeedGroupMessage> generateFeedGroupMap(List<FeedMessage> feeds) {
        Set<String> feedgroupIdSet = feeds.stream()
                .map(FeedMessage::getFeedgroupId)
                .collect(Collectors.toSet());

        Map<String, FeedGroupMessage> feedgroupMap = new HashMap<>();
        if (!feedgroupIdSet.isEmpty()) {
            feedgroupMap = this.feedGroupService.retrieveFeedGroupsByIds(feedgroupIdSet).stream()
                    .collect(Collectors.toMap(FeedGroupMessage::getId, Function.identity(), (feedgroup1, feedgroup2) -> feedgroup1, HashMap::new));
        }

        return feedgroupMap;
    }

}
