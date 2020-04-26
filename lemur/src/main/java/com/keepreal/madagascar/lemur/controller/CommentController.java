package com.keepreal.madagascar.lemur.controller;

import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.CommentApi;
import swagger.model.DummyResponse;

@RestController
public class CommentController implements CommentApi {

    public ResponseEntity<DummyResponse> apiV1CommentsIdDelete(@ApiParam(value = "id",required=true) @PathVariable("id") String id) {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
