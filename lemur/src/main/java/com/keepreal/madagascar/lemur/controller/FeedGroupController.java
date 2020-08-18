package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.lemur.service.FeedGroupService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ApiUtil;
import swagger.api.FeedGroupApi;
import swagger.model.DummyResponse;
import swagger.model.FeedGroupResponse;
import swagger.model.FeedGroupsResponse;
import swagger.model.PostFeedGroupRequest;
import swagger.model.PutFeedGroupRequest;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Represents the feed group controller.
 */
@RestController
public class FeedGroupController implements FeedGroupApi {

    private final FeedGroupService feedGroupService;

    public FeedGroupController(FeedGroupService feedGroupService) {
        this.feedGroupService = feedGroupService;
    }

    /**
     * DELETE /api/v1/feedgroups/{id} : 删除指定的作品集
     *
     * @param id id (required)
     * @return common response (status code 200)
     */
    public ResponseEntity<DummyResponse> apiV1FeedgroupsIdDelete(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * PUT /api/v1/feedgroups/{id} : 编辑指定的作品集
     *
     * @param id id (required)
     * @param putFeedGroupRequest  (required)
     * @return 单一作品集返回 (status code 200)
     */
    public ResponseEntity<FeedGroupResponse> apiV1FeedgroupsIdPut(String id,
                                                                  PutFeedGroupRequest putFeedGroupRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * GET /api/v1/islands/{id}/feedgroups : 获得所有作品集
     *
     * @param id id (required)
     * @param page page number (optional, default to 0)
     * @param pageSize size of a page (optional, default to 10)
     * @return 用户所有作品集返回 (status code 200)
     */
    public ResponseEntity<FeedGroupsResponse> apiV1IslandsIdFeedgroupsGet(String id,
                                                                          Integer page,
                                                                          Integer pageSize) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    /**
     * POST /api/v1/islands/{id}/feedgroups : 创建一个新的作品集
     *
     * @param id id (required)
     * @param postFeedGroupRequest  (required)
     * @return 单一作品集返回 (status code 200)
     */
    public ResponseEntity<FeedGroupResponse> apiV1IslandsIdFeedgroupsPost(String id,
                                                                          PostFeedGroupRequest postFeedGroupRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
