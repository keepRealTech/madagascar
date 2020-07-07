package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;
import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.stats_events.annotation.HttpStatsEventTrigger;
import com.keepreal.madagascar.lemur.dtoFactory.CommentDTOFactory;
import com.keepreal.madagascar.lemur.service.CommentService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ApiUtil;
import swagger.api.CommentApi;
import swagger.model.ChatAccessResponse;
import swagger.model.CommentResponse;
import swagger.model.CommentsResponse;
import swagger.model.DummyResponse;
import swagger.model.PostCommentRequest;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the comment controller.
 */
@RestController
public class CommentController implements CommentApi {

    private final CommentService commentService;
    private final CommentDTOFactory commentDTOFactory;
    private final FeedService feedService;
    private final IslandService islandService;

    /**
     * Constructs the comment controller.
     *
     * @param commentService    {@link CommentService}.
     * @param commentDTOFactory {@link CommentDTOFactory}.
     * @param feedService       {@link FeedService}.
     * @param islandService     {@link IslandService}.
     */
    public CommentController(CommentService commentService,
                             CommentDTOFactory commentDTOFactory,
                             FeedService feedService,
                             IslandService islandService) {
        this.commentService = commentService;
        this.commentDTOFactory = commentDTOFactory;
        this.feedService = feedService;
        this.islandService = islandService;
    }

    /**
     * Implements the delete comment by id api.
     *
     * @param id Comment id.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1CommentsIdDelete(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        CommentMessage commentMessage = this.commentService.retrieveCommentById(id);
        FeedMessage feedMessage = this.feedService.retrieveFeedById(commentMessage.getFeedId(), commentMessage.getUserId());
        IslandMessage islandMessage = islandService.retrieveIslandById(feedMessage.getIslandId());
        if (!userId.equals(commentMessage.getUserId())
                && !userId.equals(feedMessage.getUserId())
                && !userId.equals(islandMessage.getHostId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        this.commentService.deleteCommentById(id);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
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

    
    public ResponseEntity<ChatAccessResponse> apiV1UsersIdChatsGet(String id) {

    }

}
