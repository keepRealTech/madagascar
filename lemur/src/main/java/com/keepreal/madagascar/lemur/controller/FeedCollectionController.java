package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.FeedGroupMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.fossa.CollectedFeedsResponse;
import com.keepreal.madagascar.lemur.dtoFactory.FeedDTOFactory;
import com.keepreal.madagascar.lemur.service.FeedCollectionService;
import com.keepreal.madagascar.lemur.service.FeedGroupService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.FeedCollectionApi;
import swagger.model.DummyResponse;
import swagger.model.TimelinesResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class FeedCollectionController implements FeedCollectionApi {

    private final FeedCollectionService feedCollectionService;
    private final FeedGroupService feedGroupService;
    private final MembershipService membershipService;
    private final FeedDTOFactory feedDTOFactory;

    public FeedCollectionController(FeedCollectionService feedCollectionService,
                                    FeedGroupService feedGroupService,
                                    MembershipService membershipService,
                                    FeedDTOFactory feedDTOFactory) {
        this.feedCollectionService = feedCollectionService;
        this.feedGroupService = feedGroupService;
        this.membershipService = membershipService;
        this.feedDTOFactory = feedDTOFactory;
    }

    @Override
    public ResponseEntity<TimelinesResponse> apiV1FeedCollectionGet(Long minTimestamp, Long maxTimestamp, Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();

        CollectedFeedsResponse collectedFeedsResponse = this.feedCollectionService.retrieveFeedsByUserId(userId, pageSize, minTimestamp, maxTimestamp);

        Map<String, List<MembershipMessage>> feedMembershipMap = this.generateFeedMembershipMap(collectedFeedsResponse.getFeedList());

        Map<String, FeedGroupMessage> feedGroupMessageMap = this.generateFeedGroupMap(collectedFeedsResponse.getFeedList());

        TimelinesResponse response = new TimelinesResponse();
        response.setData(collectedFeedsResponse.getFeedList()
                .stream()
                .map(feedMessage -> this.feedDTOFactory.valueOf(feedMessage,
                        feedMembershipMap.getOrDefault(feedMessage.getId(), Collections.emptyList()),
                        true,
                        feedMessage.getCreatedAt(),
                        feedGroupMessageMap.get(feedMessage.getFeedgroupId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setCurrentTime(System.currentTimeMillis());
        response.setHasFeedgroup(Boolean.TRUE.equals(this.feedGroupService.existsFeedGroupsByUserId(userId)));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DummyResponse> apiV1FeedsIdFeedCollectionDelete(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        this.feedCollectionService.removeFeedToCollection(userId, id);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DummyResponse> apiV1FeedsIdFeedCollectionPost(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        this.feedCollectionService.addFeedToCollection(userId, id);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
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
