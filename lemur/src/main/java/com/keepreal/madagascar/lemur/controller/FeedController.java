package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandAccessType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.stats_events.annotation.HttpStatsEventTrigger;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.fossa.FeedGroupFeedResponse;
import com.keepreal.madagascar.fossa.FeedGroupFeedsResponse;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.lemur.converter.DefaultErrorMessageTranslater;
import com.keepreal.madagascar.lemur.converter.MediaTypeConverter;
import com.keepreal.madagascar.lemur.dtoFactory.FeedDTOFactory;
import com.keepreal.madagascar.lemur.service.FeedGroupService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.ImageService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import swagger.api.FeedApi;
import swagger.model.CheckFeedsMessage;
import swagger.model.DummyResponse;
import swagger.model.FeedDTO;
import swagger.model.FeedResponse;
import swagger.model.FeedsResponseV2;
import swagger.model.FullFeedResponse;
import swagger.model.IslandFeedSnapshotDTO;
import swagger.model.IslandFeedSnapshotResponse;
import swagger.model.MultiMediaType;
import swagger.model.PostCheckFeedsRequest;
import swagger.model.PostCheckFeedsResponse;
import swagger.model.PostFeedPayload;
import swagger.model.PostFeedRequestV2;
import swagger.model.TimelinesResponse;
import swagger.model.TopFeedRequest;
import swagger.model.ToppedFeedsDTO;
import swagger.model.TutorialDTO;
import swagger.model.TutorialResponse;

import javax.validation.Valid;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the feed controller.
 */
@RestController
@Slf4j
public class FeedController implements FeedApi {

    private static final String SUPER_ADMIN_USER_ID = "99999999";
    private static final String POSTING_INSTRUCTION_TITLE = "高清原图、长篇文章、音频和视频发布指南:";
    private static final String POSTING_INSTRUCTION_CONTENT = "1.在电脑端打开跳岛官网 tiaodaoapp.com\r\n" +
            "2.微信扫码登录\r\n" +
            "3.点击\"发布\"按钮";

    private final ImageService imageService;
    private final IslandService islandService;
    private final MembershipService membershipService;
    private final FeedService feedService;
    private final FeedGroupService feedGroupService;
    private final FeedDTOFactory feedDTOFactory;

    /**
     * Constructs the feed controller.
     *
     * @param imageService      {@link ImageService}.
     * @param feedService       {@link FeedService}.
     * @param islandService     {@link IslandService}.
     * @param membershipService {@link MembershipService}.
     * @param feedGroupService  {@link FeedGroupService}.
     * @param feedDTOFactory    {@link FeedDTOFactory}.
     */
    public FeedController(ImageService imageService,
                          FeedService feedService,
                          IslandService islandService,
                          MembershipService membershipService,
                          FeedGroupService feedGroupService,
                          FeedDTOFactory feedDTOFactory) {
        this.imageService = imageService;
        this.feedService = feedService;
        this.islandService = islandService;
        this.membershipService = membershipService;
        this.feedGroupService = feedGroupService;
        this.feedDTOFactory = feedDTOFactory;
    }

    /**
     * Implements the create feeds api.
     *
     * @param payload (required) {@link PostFeedPayload}.
     * @param images  (optional) Images.
     * @return {@link DummyResponse}.
     */
    @Deprecated
    @HttpStatsEventTrigger(
            category = StatsEventCategory.STATS_CAT_FEED,
            action = StatsEventAction.STATS_ACT_CREATE,
            label = "image number",
            metadata = "[1].size()"
    )
    public ResponseEntity<DummyResponse> apiV1FeedsPost(
            PostFeedPayload payload,
            @ApiParam(value = "file detail") @Valid @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        if (Objects.isNull(payload)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DummyResponse response = new DummyResponse();
        if (images.size() > 9) {
            DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_IMAGE_NUMBER_TOO_LARGE);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }

        if (images.size() == 0 && StringUtils.isEmpty(payload.getContent())) {
            DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_INVALID_ARGUMENT);
            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        }

        String userId = HttpContextUtils.getUserIdFromContext();

        List<String> imageUris = images
                .stream()
                .map(this.imageService::uploadSingleImage)
                .collect(Collectors.toList());

        this.feedService.createFeed(payload.getIslandIds(), payload.getMembershipIds(), userId, payload.getContent(), imageUris);

        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements delete a feed by id api.
     *
     * @param id id (required) Feed id.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1FeedsIdDelete(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        FeedMessage feedMessage = this.feedService.retrieveFeedById(id, userId);
        IslandMessage islandMessage = this.islandService.retrieveIslandById(feedMessage.getIslandId());
        if (!userId.equals(FeedController.SUPER_ADMIN_USER_ID)
                && !userId.equals(islandMessage.getHostId())
                && !userId.equals(feedMessage.getUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        this.feedService.deleteFeedById(id);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements retrieve feed by id api.
     *
     * @param id                id (required) Feed id.
     * @param includeChargeable Whether includes chargeable feeds.
     * @return {@link FullFeedResponse}.
     */
    @CrossOrigin
    @Override
    public ResponseEntity<FullFeedResponse> apiV1FeedsIdGet(String id, Boolean includeChargeable) {
        String userId = HttpContextUtils.getUserIdFromContext();
        FeedGroupFeedResponse feedGroupFeedResponse = this.feedService.retrieveFeedGroupFeedById(id, userId);

        FullFeedResponse response = new FullFeedResponse();
        response.setData(this.feedDTOFactory.valueOf(feedGroupFeedResponse.getFeed(),
                feedGroupFeedResponse.getFeedGroup(),
                feedGroupFeedResponse.getLastFeedId(),
                feedGroupFeedResponse.getNextFeedId(),
                includeChargeable));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get feeds api.
     *
     * @param islandId island id (optional) Island id.
     * @param fromHost (optional) Whether from host.
     * @param page     page number (optional, default to 0).
     * @param pageSize size of a page (optional, default to 10).
     * @return {@link FeedsResponse}.
     */
    @Deprecated
    public ResponseEntity<swagger.model.FeedsResponse> apiV1FeedsGet(String islandId,
                                                                     Boolean fromHost,
                                                                     String v,
                                                                     String osn,
                                                                     Integer page,
                                                                     Integer pageSize) {
        swagger.model.FeedsResponse response = new swagger.model.FeedsResponse();
        if ("1.0.0".equals(v) && "iOS".equals(osn)) {
            response.setRtn(ErrorCode.REQUEST_LOW_VERSION_ERROR_VALUE);
            response.setMsg(new DefaultErrorMessageTranslater().translate(ErrorCode.REQUEST_LOW_VERSION_ERROR));
            return new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
        }

        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.FeedsResponse feedsResponse =
                this.feedService.retrieveIslandFeeds(islandId, fromHost, userId, null, null, page, pageSize, false);

        Map<String, List<MembershipMessage>> feedMembershipMap = this.generateFeedMembershipMap(feedsResponse.getFeedList());

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

    /**
     * Implements the get feeds api.
     *
     * @param minTimestamp      (optional, default to 0) minimal feed created timestamp.
     * @param maxTimestamp      (optional, default to 0) maximal feed created timestamp.
     * @param pageSize          (optional, default to 10) size of a page .
     * @param includeChargeable Whether includes the chargeable feeds.
     * @return {@link TimelinesResponse}.
     */
    @Override
    public ResponseEntity<TimelinesResponse> apiV11FeedsGet(Long minTimestamp,
                                                            Long maxTimestamp,
                                                            Integer pageSize,
                                                            Boolean includeChargeable) {
        String userId = HttpContextUtils.getUserIdFromContext();
        AbstractMap.SimpleEntry<Boolean, FeedsResponse> entry =
                this.feedService.retrieveUserFeeds(userId, minTimestamp, maxTimestamp, pageSize);

        Map<String, List<MembershipMessage>> feedMembershipMap = this.generateFeedMembershipMap(entry.getValue().getFeedList());

        TimelinesResponse response = new TimelinesResponse();
        response.setData(entry.getValue().getFeedList()
                .stream()
                .map(feed -> this.feedDTOFactory.valueOf(feed,
                        feedMembershipMap.getOrDefault(feed.getId(), Collections.emptyList()),
                        includeChargeable))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(entry.getValue().getFeedCount() > 0, entry.getKey(), pageSize));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get public accessible feeds api.
     *
     * @param minTimestamp      (optional, default to 0) minimal feed created timestamp.
     * @param maxTimestamp      (optional, default to 0) maximal feed created timestamp.
     * @param pageSize          (optional, default to 10) size of a page .
     * @param includeChargeable Whether includes the chargeable feeds.
     * @return {@link TimelinesResponse}.
     */
    @Override
    public ResponseEntity<TimelinesResponse> apiV1FeedsPublicGet(Long minTimestamp,
                                                                 Long maxTimestamp,
                                                                 Integer pageSize,
                                                                 Boolean includeChargeable) {
        String userId = HttpContextUtils.getUserIdFromContext();

        AbstractMap.SimpleEntry<Boolean, List<AbstractMap.SimpleEntry<Long, FeedMessage>>> entry =
                this.feedService.retrievePublicFeeds(userId, minTimestamp, maxTimestamp, pageSize);

        Map<String, List<MembershipMessage>> feedMembershipMap = this.generateFeedMembershipMap(entry.getValue()
                .stream()
                .map(AbstractMap.SimpleEntry::getValue)
                .collect(Collectors.toList()));

        TimelinesResponse response = new TimelinesResponse();
        response.setData(entry.getValue()
                .stream()
                .map(innerEntry -> this.feedDTOFactory.valueOf(innerEntry.getValue(),
                        feedMembershipMap.getOrDefault(innerEntry.getValue().getId(), Collections.emptyList()),
                        includeChargeable,
                        innerEntry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(entry.getValue().size() > 0, entry.getKey(), pageSize));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get feeds api.
     *
     * @param id       id (required) Island id.
     * @param fromHost (optional) Whether from host.
     * @param page     page number (optional, default to 0).
     * @param pageSize size of a page (optional, default to 10).
     * @return {@link swagger.model.FeedsResponse}.
     */
    @Deprecated
    public ResponseEntity<swagger.model.FeedsResponse> apiV1IslandsIdFeedsGet(String id,
                                                                              Boolean fromHost,
                                                                              Integer page,
                                                                              Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.FeedsResponse feedsResponse =
                this.feedService.retrieveIslandFeeds(id, fromHost, userId, null, null, page, pageSize, false);

        Map<String, List<MembershipMessage>> feedMembershipMap = this.generateFeedMembershipMap(feedsResponse.getFeedList());

        swagger.model.FeedsResponse response = new swagger.model.FeedsResponse();
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

    /**
     * Implements the island feeds get v1.1 api.
     *
     * @param id           id (required) Island id.
     * @param fromHost     (optional) Whether from host.
     * @param minTimestamp timestamp after (optional, default to 0).
     * @param pageSize     size of a page (optional, default to 10).
     * @return {@link FeedsResponse}.
     */
    @Deprecated
    public ResponseEntity<TimelinesResponse> apiV11IslandsIdFeedsGet(String id,
                                                                     Boolean fromHost,
                                                                     Long minTimestamp,
                                                                     Long maxTimestamp,
                                                                     Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.FeedsResponse feedsResponse =
                this.feedService.retrieveIslandFeeds(id, fromHost, userId, minTimestamp, maxTimestamp, 0, pageSize, false);

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

    /**
     * Implements the island feeds get v1.1 api.
     *
     * @param id                id (required) Island id.
     * @param fromHost          (optional) Whether from host.
     * @param minTimestamp      timestamp after (optional, default to 0).
     * @param pageSize          size of a page (optional, default to 10).
     * @param includeChargeable Whether includes the chargeable feeds.
     * @return {@link swagger.model.FeedsResponse}.
     */
    @CrossOrigin
    @Override
    public ResponseEntity<FeedsResponseV2> apiV12IslandsIdFeedsGet(String id,
                                                                   Boolean fromHost,
                                                                   Long minTimestamp,
                                                                   Long maxTimestamp,
                                                                   Integer pageSize,
                                                                   Boolean includeChargeable) {
        String userId = HttpContextUtils.getUserIdFromContext();
        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);

        if (IslandAccessType.ISLAND_ACCESS_PRIVATE.equals(islandMessage.getIslandAccessType())
                && !this.islandService.checkIslandSubscription(id, userId)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_ISLAND_USER_NOT_SUBSCRIBED_ERROR);
        }

        com.keepreal.madagascar.fossa.FeedsResponse normalFeedsResponse =
                this.feedService.retrieveIslandFeeds(id, fromHost, userId, minTimestamp, maxTimestamp, 0, pageSize, true);

        Map<String, List<MembershipMessage>> feedMembershipMap = this.generateFeedMembershipMap(normalFeedsResponse.getFeedList());

        FeedsResponseV2 response = new FeedsResponseV2();
        ToppedFeedsDTO dto = new ToppedFeedsDTO();

        dto.setFeeds(normalFeedsResponse.getFeedList()
                .stream()
                .map(feed -> this.feedDTOFactory.valueOf(feed,
                        feedMembershipMap.getOrDefault(feed.getId(), Collections.emptyList()),
                        includeChargeable))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        List<FeedDTO> topFeeds = new ArrayList<>();
        if (minTimestamp == null && maxTimestamp == null) {
            com.keepreal.madagascar.fossa.FeedResponse toppedFeedResponse = this.feedService.retrieveIslandToppedFeeds(id, userId);

            Map<String, List<MembershipMessage>> toppedfeedMembershipMap = this.generateFeedMembershipMap(Collections.singletonList(toppedFeedResponse.getFeed()));

            if (toppedFeedResponse.hasFeed()) {
                FeedDTO feedDTO = this.feedDTOFactory.valueOf(toppedFeedResponse.getFeed(),
                        toppedfeedMembershipMap.getOrDefault(toppedFeedResponse.getFeed().getId(), Collections.emptyList()));
                topFeeds.add(feedDTO);
            }
        }

        dto.setToppedFeeds(topFeeds);

        response.setData(dto);
        response.setCurrentTime(System.currentTimeMillis());
        response.setPageInfo(PaginationUtils.getPageInfo(normalFeedsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the check new feeds api for polling.
     *
     * @param postCheckFeedsRequest (required) {@link PostCheckFeedsRequest}.
     * @return {@link PostCheckFeedsResponse}.
     */
    @Override
    public ResponseEntity<PostCheckFeedsResponse> apiV1FeedsCheckPost(PostCheckFeedsRequest postCheckFeedsRequest) {
        List<CheckNewFeedsMessage> checkNewFeedsMessages = this.islandService.checkNewFeeds(
                postCheckFeedsRequest.getCheckFeedsMessage().stream().map(CheckFeedsMessage::getIslandId).collect(Collectors.toList()),
                postCheckFeedsRequest.getCheckFeedsMessage().stream().map(CheckFeedsMessage::getTimestamp).collect(Collectors.toList()));

        PostCheckFeedsResponse response = new PostCheckFeedsResponse();
        response.setData(checkNewFeedsMessages
                .stream()
                .map(this.feedDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Implement the island top feed api v1 api
     *
     * @param id             id (required)  island id
     * @param topFeedRequest (required) {@link TopFeedRequest}.
     * @return {@link FeedResponse}.
     */
    @Override
    public ResponseEntity<FeedResponse> apiV1IslandsIdFeedsTopPost(String id, TopFeedRequest topFeedRequest) {
        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);
        String userId = HttpContextUtils.getUserIdFromContext();
        String hostId = islandMessage.getHostId();
        FeedMessage feedMessage = this.feedService.retrieveFeedById(topFeedRequest.getFeedId(), userId);

        if (!userId.equals(hostId) || !islandMessage.getId().equals(feedMessage.getIslandId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        this.feedService.topFeedByRequest(topFeedRequest, id);

        Map<String, List<MembershipMessage>> toppedfeedMembershipMap = this.generateFeedMembershipMap(Collections.singletonList(feedMessage));

        FeedResponse response = new FeedResponse();
        response.setData(this.feedDTOFactory.valueOf(feedMessage, toppedfeedMembershipMap.getOrDefault(feedMessage.getId(), Collections.emptyList())));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the feed advanced posting tutorial get.
     *
     * @return {@link TutorialResponse}.
     */
    @Override
    public ResponseEntity<TutorialResponse> apiV1FeedsTutorialGet() {
        TutorialDTO tutorialDTO = new TutorialDTO();
        tutorialDTO.setTitle(FeedController.POSTING_INSTRUCTION_TITLE);
        tutorialDTO.setContent(FeedController.POSTING_INSTRUCTION_CONTENT);

        TutorialResponse response = new TutorialResponse();
        response.setData(tutorialDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the feed group get feeds api.
     *
     * @param id                id (required) Feed group id.
     * @param page              page number (optional, default to 0) Page.
     * @param pageSize          size of a page (optional, default to 10) Page size.
     * @param includeChargeable Whether includes chargeable.
     * @return {@link FeedsResponse}.
     */
    @Override
    public ResponseEntity<swagger.model.FeedsResponse> apiV1FeedgroupsIdFeedsGet(String id,
                                                                                 Integer page,
                                                                                 Integer pageSize,
                                                                                 Boolean includeChargeable) {
        String userId = HttpContextUtils.getUserIdFromContext();
        FeedGroupFeedsResponse feedGroupFeedsResponse = this.feedGroupService.retrieveFeedGroupFeeds(id, userId, page, pageSize);

        Map<String, List<MembershipMessage>> feedMembershipMap = this.generateFeedMembershipMap(feedGroupFeedsResponse.getFeedList());

        swagger.model.FeedsResponse response = new swagger.model.FeedsResponse();
        response.setData(feedGroupFeedsResponse.getFeedList()
                .stream()
                .map(feed -> this.feedDTOFactory.valueOf(feed,
                        feedMembershipMap.getOrDefault(feed.getId(), Collections.emptyList()),
                        includeChargeable))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setCurrentTime(System.currentTimeMillis());
        response.setPageInfo(PaginationUtils.getPageInfo(feedGroupFeedsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Posts new feed.
     *
     * @param postFeedRequestV2 (required)
     * @return {@link DummyResponse}.
     */
    @CrossOrigin
    @Override
    public ResponseEntity<DummyResponse> apiV11FeedsPost(PostFeedRequestV2 postFeedRequestV2) {
        String userId = HttpContextUtils.getUserIdFromContext();
        MultiMediaType mediaType = postFeedRequestV2.getMediaType();
        DummyResponse response = new DummyResponse();
        switch (mediaType) {
            case PICS:
                if (postFeedRequestV2.getMultimedia().size() > 9) {
                    DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_IMAGE_NUMBER_TOO_LARGE);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                break;
            case ALBUM:
                if (postFeedRequestV2.getMultimedia().size() > 18) {
                    DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_IMAGE_NUMBER_TOO_LARGE);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                break;
            case VIDEO:
            case AUDIO:
                if (CollectionUtils.isEmpty(postFeedRequestV2.getMultimedia()) ||
                        StringUtils.isEmpty(postFeedRequestV2.getMultimedia().get(0).getVideoId())) {
                    DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_INVALID_ARGUMENT);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                break;
            case HTML:
                if (StringUtils.isEmpty(postFeedRequestV2.getText())) {
                    DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_INVALID_ARGUMENT);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                break;
            case TEXT:
                break;
            default:
                DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_INVALID_ARGUMENT);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!CollectionUtils.isEmpty(postFeedRequestV2.getMembershipIds()) && postFeedRequestV2.getPriceInCents() != null && postFeedRequestV2.getPriceInCents() > 0L) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        this.feedService.createFeedV2(postFeedRequestV2.getIslandIds(),
                postFeedRequestV2.getMembershipIds(),
                userId,
                MediaTypeConverter.convertToMediaType(mediaType),
                postFeedRequestV2.getMultimedia(),
                postFeedRequestV2.getText(),
                postFeedRequestV2.getFeedGroupId(),
                postFeedRequestV2.getPriceInCents());

        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the api for unsubscribed island feeds.
     *
     * @param id id (required) Island id.
     * @return {@link IslandFeedSnapshotResponse}.
     */
    @CrossOrigin
    @Override
    public ResponseEntity<IslandFeedSnapshotResponse> apiV1IslandsIdFeedsSnapshotGet(String id) {
        Integer count = this.feedService.retrieveFeedCountByIslandId(id);
        List<String> imageUris = this.islandService.retrieveIslanderPortraitUrlByIslandId(id);

        IslandFeedSnapshotDTO dto = new IslandFeedSnapshotDTO();
        dto.setFeedCount(count);
        dto.setUserPortraitUris(imageUris);

        IslandFeedSnapshotResponse response = new IslandFeedSnapshotResponse();
        response.setData(dto);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DummyResponse> apiV1FeedsIdSaveAuthorityPut(String id, @Valid Boolean canSave) {
        String userId = HttpContextUtils.getUserIdFromContext();
        DummyResponse response = new DummyResponse();

        FeedMessage feedMessage = this.feedService.retrieveFeedById(id, userId);
        if (!userId.equals(feedMessage.getHostId()) || !userId.equals(feedMessage.getUserId())) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_FORBIDDEN);
        }

        this.feedService.updateFeedSaveAuthority(id, canSave);
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

}
