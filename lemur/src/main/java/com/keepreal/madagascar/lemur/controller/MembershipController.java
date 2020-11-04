package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.lemur.dtoFactory.FeedDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.MembershipDTOFactory;
import com.keepreal.madagascar.lemur.service.FeedChargeService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.SubscribeMembershipService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.MembershipApi;
import swagger.model.DeactivateMembershipRequest;
import swagger.model.DummyResponse;
import swagger.model.FeedMembershipsResponse;
import swagger.model.MembershipResponse;
import swagger.model.MembershipTemplatesResponse;
import swagger.model.MembershipsResponse;
import swagger.model.MyMembershipsResponse;
import swagger.model.PostMembershipRequest;
import swagger.model.PutMembershipRequest;
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
 * Represents the membership controller.
 */
@RestController
public class MembershipController implements MembershipApi {

    private final MembershipService membershipService;
    private final MembershipDTOFactory membershipDTOFactory;
    private final FeedService feedService;
    private final FeedDTOFactory feedDTOFactory;
    private final SubscribeMembershipService subscribeMembershipService;
    private final FeedChargeService feedChargeService;

    /**
     * Constructs the membership controller.
     *
     * @param membershipService          {@link MembershipService}.
     * @param membershipDTOFactory       {@link MembershipDTOFactory}.
     * @param feedService                {@link FeedService}.
     * @param feedDTOFactory             {@link FeedDTOFactory}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     * @param feedChargeService          {@link FeedChargeService}.
     */
    public MembershipController(MembershipService membershipService,
                                MembershipDTOFactory membershipDTOFactory,
                                FeedService feedService,
                                FeedDTOFactory feedDTOFactory,
                                SubscribeMembershipService subscribeMembershipService,
                                FeedChargeService feedChargeService) {
        this.membershipService = membershipService;
        this.membershipDTOFactory = membershipDTOFactory;
        this.feedService = feedService;
        this.feedDTOFactory = feedDTOFactory;
        this.subscribeMembershipService = subscribeMembershipService;
        this.feedChargeService = feedChargeService;
    }

    /**
     * Implements the get feed memberships api.
     *
     * @param id island id.
     * @return {@link FeedMembershipsResponse}.
     */
    @Override
    public ResponseEntity<FeedMembershipsResponse> apiV1IslandsIdFeedMembershipsGet(String id) {
        List<FeedMembershipMessage> feedMembershipMessages = membershipService.retrieveFeedMembershipsByIslandId(id);

        FeedMembershipsResponse response = new FeedMembershipsResponse();
        response.data(feedMembershipMessages.stream().map(membershipDTOFactory::feedValueOf).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get memberships api(with top flag and color type).
     *
     * @param id island id.
     * @return {@link MembershipResponse}.
     */
    @Override
    public ResponseEntity<MembershipsResponse> apiV1IslandsIdMembershipsGet(String id) {
        List<MembershipMessage> membershipMessages = membershipService.retrieveMembershipsByIslandId(id, true);

        MembershipsResponse response = new MembershipsResponse();
        response.data(membershipMessages.stream().map(membershipDTOFactory::valueOf).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the membership api.
     *
     * @param id                    island id.
     * @param postMembershipRequest {@link PostMembershipRequest}.
     * @return {@link MembershipResponse}.
     */
    @Override
    public ResponseEntity<MembershipResponse> apiV1IslandsIdMembershipsPost(String id, @Valid PostMembershipRequest postMembershipRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        if (postMembershipRequest.getName().length() >= Constants.MEMBERSHIP_NAME_MAX_LENGTH) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_MEMBERSHIP_NAME_TOO_LONG_ERROR);
        }

        MembershipMessage membershipMessage = membershipService.createMembership(postMembershipRequest.getName(),
                postMembershipRequest.getChargePerMonth(),
                postMembershipRequest.getDescription(),
                id,
                userId,
                Objects.nonNull(postMembershipRequest.getUseCustomMessage()) ? postMembershipRequest.getUseCustomMessage() : false,
                Objects.nonNull(postMembershipRequest.getMessage()) ? postMembershipRequest.getMessage() : "",
                Objects.nonNull(postMembershipRequest.getIsPermanent()) ? postMembershipRequest.getIsPermanent() : false,
                Objects.nonNull(postMembershipRequest.getImageUri()) ? postMembershipRequest.getImageUri() : "",
                Objects.nonNull(postMembershipRequest.getWidth()) ? postMembershipRequest.getWidth() : 0,
                Objects.nonNull(postMembershipRequest.getHeight()) ? postMembershipRequest.getHeight() : 0,
                Objects.nonNull(postMembershipRequest.getSize()) ? postMembershipRequest.getSize() : 0);

        MembershipResponse response = new MembershipResponse();
        response.data(membershipDTOFactory.briefValueOf(membershipMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the deactivate membership api.
     *
     * @param id membership id.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1MembershipsIdDeactivatePut(String id, @Valid DeactivateMembershipRequest deactivateMembershipRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        membershipService.deactivateMembershipById(id, userId, deactivateMembershipRequest.getIsDeactivate());
        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the delete membership api.
     *
     * @param id membership id.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1MembershipsIdDelete(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        membershipService.deleteMembershipById(id, userId);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements get membership api.
     *
     * @param id membership id.
     * @return {@link MembershipResponse}.
     */
    @Override
    public ResponseEntity<MembershipResponse> apiV1MembershipsIdGet(String id) {
        MembershipMessage membershipMessage = membershipService.retrieveMembershipById(id);

        MembershipResponse response = new MembershipResponse();
        response.data(membershipDTOFactory.briefValueOf(membershipMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the update membership api.
     *
     * @param id                   membership id.
     * @param putMembershipRequest {@link PutMembershipRequest}.
     * @return {@link MembershipResponse}.
     */
    @Override
    public ResponseEntity<MembershipResponse> apiV1MembershipsIdPut(String id, PutMembershipRequest putMembershipRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        MembershipMessage membershipMessage = this.membershipService.updateMembershipById(id,
                putMembershipRequest.getName(),
                putMembershipRequest.getDescription(),
                putMembershipRequest.getChargePerMonth(),
                userId,
                putMembershipRequest.getUseCustomMessage(),
                putMembershipRequest.getMessage(),
                putMembershipRequest.getIsPermanent(),
                putMembershipRequest.getImageUri(),
                putMembershipRequest.getWidth(),
                putMembershipRequest.getHeight(),
                putMembershipRequest.getSize());

        MembershipResponse response = new MembershipResponse();
        response.data(membershipDTOFactory.briefValueOf(membershipMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the top membership api.
     *
     * @param id       membership id.
     * @param isRevoke whether is revoking
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1MembershipsIdTopPost(String id, Boolean isRevoke) {
        String userId = HttpContextUtils.getUserIdFromContext();
        membershipService.topMembershipById(id, isRevoke, userId);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get membership template api.
     *
     * @return {@link MembershipTemplatesResponse}.
     */
    @Override
    public ResponseEntity<MembershipTemplatesResponse> apiV1MembershipsTemplatesGet() {
        MembershipTemplatesResponse response = new MembershipTemplatesResponse();
        response.setData(this.membershipDTOFactory.listValueOf());
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get my membership api.
     *
     * @param id island id.
     * @return {@link MyMembershipsResponse}
     */
    @Override
    public ResponseEntity<MyMembershipsResponse> apiV1IslandsIdMyMembershipsGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        MyMembershipsResponse response = new MyMembershipsResponse();
        response.setData(this.membershipDTOFactory.valueOf(userId, id));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get membership feed api.
     *
     * @param id           island id
     * @param minTimestamp timestamp after (optional)
     * @param maxTimestamp timestamp before (optional)
     * @param pageSize     size of a page (optional, default to 10)
     * @return {@link TimelinesResponse}
     */
    @Override
    public ResponseEntity<TimelinesResponse> apiV1IslandsIdMembershipFeedsGet(String id, Long minTimestamp, Long maxTimestamp, Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();

        List<String> feedIds = this.feedChargeService.retrieveHasAccessFeedIds(userId, id, maxTimestamp, minTimestamp);
        List<String> membershipIds = this.subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(id, userId);
        if (feedIds.isEmpty() && membershipIds.isEmpty()) {
            TimelinesResponse response = new TimelinesResponse();
            response.setData(Collections.emptyList());
            response.setCurrentTime(System.currentTimeMillis());
            response.setPageInfo(PaginationUtils.getPageInfo(false, false, pageSize));
            response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
            response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        FeedsResponse feedsResponse = this.feedService.retrieveIslandMembershipFeeds(userId, id, minTimestamp, maxTimestamp, pageSize, feedIds, membershipIds);
        Map<String, List<MembershipMessage>> feedMembershipMap = this.generateFeedMembershipMap(feedsResponse.getFeedList());

        TimelinesResponse response = new TimelinesResponse();
        response.setData(feedsResponse.getFeedList()
                .stream()
                .map(feed -> this.feedDTOFactory.valueOf(feed,
                        feedMembershipMap.getOrDefault(feed.getId(), Collections.emptyList())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setCurrentTime(System.currentTimeMillis());
        response.setPageInfo(PaginationUtils.getPageInfo(feedsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

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
}
