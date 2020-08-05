package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.stats_events.annotation.HttpStatsEventTrigger;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.lemur.converter.DefaultErrorMessageTranslater;
import com.keepreal.madagascar.lemur.converter.MediaTypeConverter;
import com.keepreal.madagascar.lemur.dtoFactory.FeedDTOFactory;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.ImageService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import swagger.api.FeedApi;
import swagger.model.CheckFeedsMessage;
import swagger.model.DummyResponse;
import swagger.model.FeedDTO;
import swagger.model.FeedResponse;
import swagger.model.FeedsResponseV2;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the feed controller.
 */
@RestController
public class FeedController implements FeedApi {

    private static final String SUPER_ADMIN_USER_ID = "99999999";
    private static final String POSTING_INSTRUCTION_TITLE = "高清原图、长篇文章、音频和视频发布指南:";
    private static final String POSTING_INSTRUCTION_CONTENT = "1.在电脑端打开跳岛官网 https://home.keepreal.cn/\r\n" +
            "2.微信扫码登录\r\n" +
            "3.点击\"发布\"按钮";

    private final ImageService imageService;
    private final FeedService feedService;
    private final IslandService islandService;
    private final FeedDTOFactory feedDTOFactory;

    /**
     * Constructs the feed controller.
     *
     * @param imageService   {@link ImageService}.
     * @param feedService    {@link FeedService}.
     * @param islandService  {@link IslandService}.
     * @param feedDTOFactory {@link FeedDTOFactory}.
     */
    public FeedController(ImageService imageService,
                          FeedService feedService,
                          IslandService islandService,
                          FeedDTOFactory feedDTOFactory) {
        this.imageService = imageService;
        this.feedService = feedService;
        this.islandService = islandService;
        this.feedDTOFactory = feedDTOFactory;
    }

    /**
     * Implements the create feeds api.
     *
     * @param payload (required) {@link PostFeedPayload}.
     * @param images  (optional) Images.
     * @return {@link DummyResponse}.
     */
    @Override
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
     * @param id id (required) Feed id.
     * @return {@link FeedResponse}.
     */
    @Override
    public ResponseEntity<FeedResponse> apiV1FeedsIdGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        FeedMessage feedMessage = this.feedService.retrieveFeedById(id, userId);

        FeedResponse response = new FeedResponse();
        response.setData(this.feedDTOFactory.valueOf(feedMessage));
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
    @Override
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

        response.setData(feedsResponse.getFeedList()
                .stream()
                .map(this.feedDTOFactory::valueOf)
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
     * @param minTimestamp (optional, default to 0) minimal feed created timestamp.
     * @param maxTimestamp (optional, default to 0) maximal feed created timestamp.
     * @param pageSize     (optional, default to 10) size of a page .
     * @return {@link TimelinesResponse}.
     */
    @Override
    public ResponseEntity<TimelinesResponse> apiV11FeedsGet(Long minTimestamp,
                                                            Long maxTimestamp,
                                                            Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        AbstractMap.SimpleEntry<Boolean, FeedsResponse> entry =
                this.feedService.retrieveUserFeeds(userId, minTimestamp, maxTimestamp, pageSize);

        TimelinesResponse response = new TimelinesResponse();
        response.setData(entry.getValue().getFeedList()
                .stream()
                .map(this.feedDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(entry.getValue().getFeedCount() > 0, entry.getKey(), pageSize));
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
    @Override
    public ResponseEntity<swagger.model.FeedsResponse> apiV1IslandsIdFeedsGet(String id,
                                                                              Boolean fromHost,
                                                                              Integer page,
                                                                              Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.FeedsResponse feedsResponse =
                this.feedService.retrieveIslandFeeds(id, fromHost, userId, null, null, page, pageSize, false);

        swagger.model.FeedsResponse response = new swagger.model.FeedsResponse();
        response.setData(feedsResponse.getFeedList()
                .stream()
                .map(this.feedDTOFactory::valueOf)
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
    @Override
    public ResponseEntity<TimelinesResponse> apiV11IslandsIdFeedsGet(String id,
                                                                     Boolean fromHost,
                                                                     Long minTimestamp,
                                                                     Long maxTimestamp,
                                                                     Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.FeedsResponse feedsResponse =
                this.feedService.retrieveIslandFeeds(id, fromHost, userId, minTimestamp, maxTimestamp, 0, pageSize, false);

        TimelinesResponse response = new TimelinesResponse();
        response.setData(feedsResponse.getFeedList()
                .stream()
                .map(this.feedDTOFactory::valueOf)
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
     * @return {@link swagger.model.FeedsResponse}.
     */
    @Override
    public ResponseEntity<FeedsResponseV2> apiV12IslandsIdFeedsGet(String id,
                                                                   Boolean fromHost,
                                                                   Long minTimestamp,
                                                                   Long maxTimestamp,
                                                                   Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.FeedsResponse normalFeedsResponse =
                this.feedService.retrieveIslandFeeds(id, fromHost, userId, minTimestamp, maxTimestamp, 0, pageSize, true);

        FeedsResponseV2 response = new FeedsResponseV2();
        ToppedFeedsDTO dto = new ToppedFeedsDTO();

        dto.setFeeds(normalFeedsResponse.getFeedList()
                .stream()
                .map(this.feedDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        List<FeedDTO> topFeeds = new ArrayList<>();
        if (minTimestamp == null && maxTimestamp == null) {
            com.keepreal.madagascar.fossa.FeedResponse toppedFeedResponse = this.feedService.retrieveIslandToppedFeeds(id, userId);
            if (toppedFeedResponse.hasFeed()) {
                FeedDTO feedDTO = this.feedDTOFactory.valueOf(toppedFeedResponse.getFeed());
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

        FeedResponse response = new FeedResponse();
        response.setData(this.feedDTOFactory.valueOf(feedMessage));
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

        this.feedService.createFeedV2(postFeedRequestV2.getIslandIds(), postFeedRequestV2.getMembershipIds(), userId, MediaTypeConverter.convertToMediaType(mediaType), postFeedRequestV2.getMultimedia(), postFeedRequestV2.getText());

        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
