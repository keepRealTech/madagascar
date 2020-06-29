package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.service.CommentService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.CommentApi;
import swagger.model.DummyResponse;

/**
 * Represents the comment controller.
 */
@RestController
public class CommentController implements CommentApi {

    private final CommentService commentService;
    private final FeedService feedService;
    private final IslandService islandService;

    /**
     * Constructs the comment controller.
     *
     * @param commentService {@link CommentService}.
     * @param feedService    {@link FeedService}.
     * @param islandService  {@link IslandService}.
     */
    public CommentController(CommentService commentService,
                             FeedService feedService, IslandService islandService) {
        this.commentService = commentService;
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

}
