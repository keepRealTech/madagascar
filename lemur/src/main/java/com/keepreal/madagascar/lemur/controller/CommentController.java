package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.lemur.service.CommentService;
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

    /**
     * Constructs the comment controller.
     *
     * @param commentService {@link CommentService}.
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Implements the delete comment by id api.
     *
     * @param id Comment id.
     * @return {@link DummyResponse}.
     */
    public ResponseEntity<DummyResponse> apiV1CommentsIdDelete(String id) {
        this.commentService.deleteCommentById(id);

        DummyResponse response = new DummyResponse();
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
