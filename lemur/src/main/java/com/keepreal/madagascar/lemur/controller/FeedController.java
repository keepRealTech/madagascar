package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;
import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.stats_events.annotation.HttpStatsEventTrigger;
import com.keepreal.madagascar.fossa.FeedRepostMessage;
import com.keepreal.madagascar.fossa.FeedRepostsResponse;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.lemur.dtoFactory.CommentDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.FeedDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.ReactionDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.RepostDTOFactory;
import com.keepreal.madagascar.lemur.service.CommentService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.ImageService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.ReactionService;
import com.keepreal.madagascar.lemur.service.RepostService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import swagger.api.FeedApi;
import swagger.model.CheckFeedsMessage;
import swagger.model.CommentResponse;
import swagger.model.CommentsResponse;
import swagger.model.DummyResponse;
import swagger.model.FeedResponse;
import swagger.model.FeedsResponse;
import swagger.model.PostCheckFeedsRequest;
import swagger.model.PostCheckFeedsResponse;
import swagger.model.PostCommentRequest;
import swagger.model.PostFeedPayload;
import swagger.model.PostReactionRequest;
import swagger.model.PostRepostRequest;
import swagger.model.ReactionResponse;
import swagger.model.ReactionsResponse;
import swagger.model.RepostResponse;
import swagger.model.RepostsResponse;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the feed controller.
 */
@RestController
public class FeedController implements FeedApi {

    private final ImageService imageService;
    private final FeedService feedService;
    private final IslandService islandService;
    private final RepostService repostService;
    private final CommentService commentService;
    private final ReactionService reactionService;
    private final CommentDTOFactory commentDTOFactory;
    private final RepostDTOFactory repostDTOFactory;
    private final FeedDTOFactory feedDTOFactory;
    private final ReactionDTOFactory reactionDTOFactory;

    /**
     * Constructs the feed controller.
     *
     * @param imageService       {@link ImageService}.
     * @param feedService        {@link FeedService}.
     * @param islandService      {@link IslandService}.
     * @param repostService      {@link RepostService}.
     * @param commentService     {@link CommentService}.
     * @param reactionService    {@link ReactionService}.
     * @param commentDTOFactory  {@link CommentDTOFactory}.
     * @param repostDTOFactory   {@link RepostDTOFactory}.
     * @param feedDTOFactory     {@link FeedDTOFactory}.
     * @param reactionDTOFactory {@link ReactionDTOFactory}.
     */
    public FeedController(ImageService imageService,
                          FeedService feedService,
                          IslandService islandService,
                          RepostService repostService,
                          CommentService commentService,
                          ReactionService reactionService,
                          CommentDTOFactory commentDTOFactory,
                          RepostDTOFactory repostDTOFactory,
                          FeedDTOFactory feedDTOFactory,
                          ReactionDTOFactory reactionDTOFactory) {
        this.imageService = imageService;
        this.feedService = feedService;
        this.islandService = islandService;
        this.repostService = repostService;
        this.commentService = commentService;
        this.reactionService = reactionService;
        this.commentDTOFactory = commentDTOFactory;
        this.repostDTOFactory = repostDTOFactory;
        this.feedDTOFactory = feedDTOFactory;
        this.reactionDTOFactory = reactionDTOFactory;
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

        this.feedService.createFeed(payload.getIslandIds(), userId, payload.getContent(), imageUris);

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
        if (!userId.equals(islandMessage.getHostId()) && !userId.equals(feedMessage.getUserId())) {
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
    @Override
    public ResponseEntity<FeedsResponse> apiV1FeedsGet(String islandId,
                                                       Boolean fromHost,
                                                       Integer page,
                                                       Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();
        com.keepreal.madagascar.fossa.FeedsResponse feedsResponse =
                this.feedService.retrieveFeeds(islandId, fromHost, userId, page, pageSize);

        FeedsResponse response = new FeedsResponse();
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
     * Implements the get comment api.
     *
     * @param id       id (required) Feed id.
     * @param page     page number (optional, default to 0).
     * @param pageSize size of a page (optional, default to 10).
     * @return {@link CommentsResponse}.
     */
    @Override
    public ResponseEntity<CommentsResponse> apiV1FeedsIdCommentsGet(String id, Integer page, Integer pageSize) {
        com.keepreal.madagascar.fossa.CommentsResponse commentsResponse =
                this.commentService.retrieveCommentsByFeedId(id, page, pageSize);

        CommentsResponse response = new CommentsResponse();
        response.setData(commentsResponse.getCommentsList()
                .stream()
                .map(this.commentDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(commentsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the post new comment api.
     *
     * @param id                 id (required) Feed id.
     * @param postCommentRequest (required) {@link PostCommentRequest}.
     * @return {@link CommentResponse}.
     */
    @Override
    @HttpStatsEventTrigger(
            category = StatsEventCategory.STATS_CAT_COMMENT,
            action = StatsEventAction.STATS_ACT_CREATE
    )
    public ResponseEntity<CommentResponse> apiV1FeedsIdCommentsPost(String id, PostCommentRequest postCommentRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        CommentMessage commentMessage = this.commentService.createComment(
                id, userId, postCommentRequest.getContent(), postCommentRequest.getReplyToId());

        CommentResponse response = new CommentResponse();
        response.setData(this.commentDTOFactory.valueOf(commentMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get reactions by feed id api.
     *
     * @param id       id (required) Feed id.
     * @param page     page number (optional, default to 0).
     * @param pageSize size of a page (optional, default to 10).
     * @return {@link ReactionsResponse}.
     */
    @Override
    public ResponseEntity<ReactionsResponse> apiV1FeedsIdReactionsGet(String id, Integer page, Integer pageSize) {
        com.keepreal.madagascar.fossa.ReactionsResponse reactionsResponse =
                this.reactionService.retrieveReactionsByFeedId(id, page, pageSize);

        ReactionsResponse response = new ReactionsResponse();
        response.setData(reactionsResponse.getReactionsList()
                .stream()
                .map(this.reactionDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(reactionsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the post or revoke reaction api.
     *
     * @param id                  id (required) Feed id.
     * @param postReactionRequest (required) {@link PostReactionRequest}.
     * @param isRevoke            whether is revoking a reaction (optional) Whether create or revoke.
     * @return {@link ReactionResponse}.
     */
    @Override
    public ResponseEntity<ReactionResponse> apiV1FeedsIdReactionsPost(
            String id,
            PostReactionRequest postReactionRequest,
            @RequestParam(value = "isRevoke", required = false, defaultValue = "false") Boolean isRevoke) {
        String userId = HttpContextUtils.getUserIdFromContext();

        ReactionMessage reactionMessage;
        if (isRevoke) {
            reactionMessage = this.reactionService.revokeReaction(
                    id, userId, postReactionRequest.getReactions().stream().map(this::convertType).collect(Collectors.toList()));
        } else {
            reactionMessage = this.reactionService.createReaction(
                    id, userId, postReactionRequest.getReactions().stream().map(this::convertType).collect(Collectors.toList()));
        }

        ReactionResponse response = new ReactionResponse();
        response.setData(this.reactionDTOFactory.valueOf(reactionMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get feed reposts api.
     *
     * @param id       id (required) Feed id.
     * @param page     page number (optional, default to 0).
     * @param pageSize size of a page (optional, default to 10).
     * @return {@link RepostsResponse}.
     */
    @Override
    public ResponseEntity<RepostsResponse> apiV1FeedsIdRepostsGet(String id, Integer page, Integer pageSize) {
        FeedRepostsResponse repostsResponse = this.repostService.retrieveRepostFeedById(id, page, pageSize);

        RepostsResponse response = new RepostsResponse();
        response.setData(repostsResponse.getFeedRepostsList()
                .stream()
                .map(this.repostDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(repostsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the create repost feed api.
     *
     * @param id                id (required) Feed id.
     * @param postRepostRequest (required) {@link PostRepostRequest}.
     * @return {@link RepostResponse}.
     */
    @Override
    public ResponseEntity<RepostResponse> apiV1FeedsIdRepostsPost(String id, PostRepostRequest postRepostRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        FeedRepostMessage feedRepostMessage = this.repostService.createRepostFeedById(
                id, userId, postRepostRequest.getContent(), postRepostRequest.getIsSuccessful());

        RepostResponse response = new RepostResponse();
        response.setData(this.repostDTOFactory.valueOf(feedRepostMessage));
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
     * Converts {@link swagger.model.ReactionType} into {@link ReactionType}.
     *
     * @param type {@link swagger.model.ReactionType}.
     * @return {@link ReactionType}.
     */
    private ReactionType convertType(swagger.model.ReactionType type) {
        if (Objects.isNull(type)) {
            return null;
        }

        switch (type) {
            case REACTION_LIKE:
                return ReactionType.REACTION_LIKE;
            default:
                return ReactionType.UNRECOGNIZED;
        }
    }

}
