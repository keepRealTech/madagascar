package com.keepreal.madagascar.lemur.controller;

import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import swagger.api.FeedApi;
import swagger.model.BriefFeedResponse;
import swagger.model.CommentResponse;
import swagger.model.CommentsResponse;
import swagger.model.DummyResponse;
import swagger.model.FeedResponse;
import swagger.model.FeedsResponse;
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

/**
 * Represents the feed controller.
 */
@RestController
public class FeedController implements FeedApi {

    @Override
    public ResponseEntity<BriefFeedResponse> apiV1FeedsPost(
            PostFeedPayload payload,
            @ApiParam(value = "file detail") @Valid @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<FeedsResponse> apiV1FeedsGet(String islandId,
                                                       Boolean fromHost,
                                                       Integer page,
                                                       Integer pageSize) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<CommentsResponse> apiV1FeedsIdCommentsGet(String id, Integer page, Integer pageSize) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<CommentResponse> apiV1FeedsIdCommentsPost(String id, PostCommentRequest postCommentRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<DummyResponse> apiV1FeedsIdDelete(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<FeedResponse> apiV1FeedsIdGet(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<ReactionsResponse> apiV1FeedsIdReactionsGet(String id, Integer page, Integer pageSize) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<ReactionResponse> apiV1FeedsIdReactionsPost(String id, PostReactionRequest postReactionRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<RepostsResponse> apiV1FeedsIdRepostsGet(String id, Integer page, Integer pageSize) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<RepostResponse> apiV1FeedsIdRepostsPost(String id, PostRepostRequest postRepostRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
