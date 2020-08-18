package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.fossa.FeedGroupMessage;
import com.keepreal.madagascar.lemur.dtoFactory.FeedGroupDTOFactory;
import com.keepreal.madagascar.lemur.service.FeedGroupService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.FeedGroupApi;
import swagger.model.DummyResponse;
import swagger.model.FeedGroupResponse;
import swagger.model.FeedGroupsResponse;
import swagger.model.PostFeedGroupRequest;
import swagger.model.PutFeedGroupRequest;

import java.util.stream.Collectors;

/**
 * Represents the feed group controller.
 */
@RestController
public class FeedGroupController implements FeedGroupApi {

    private final FeedGroupService feedGroupService;
    private final FeedGroupDTOFactory feedGroupDTOFactory;

    /**
     * Constructs the feed group controller.
     *
     * @param feedGroupService      {@link FeedGroupService}.
     * @param feedGroupDTOFactory   {@link FeedGroupDTOFactory}.
     */
    public FeedGroupController(FeedGroupService feedGroupService,
                               FeedGroupDTOFactory feedGroupDTOFactory) {
        this.feedGroupService = feedGroupService;
        this.feedGroupDTOFactory = feedGroupDTOFactory;
    }

    /**
     * DELETE /api/v1/feedgroups/{id} : 删除指定的作品集
     *
     * @param id id (required)
     * @return common response (status code 200)
     */
    @CrossOrigin
    @Override
    public ResponseEntity<DummyResponse> apiV1FeedgroupsIdDelete(String id) {
        this.feedGroupService.deleteFeedGroupById(id);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * PUT /api/v1/feedgroups/{id} : 编辑指定的作品集
     *
     * @param id id (required)
     * @param putFeedGroupRequest  (required)
     * @return 单一作品集返回 (status code 200)
     */
    @CrossOrigin
    @Override
    public ResponseEntity<FeedGroupResponse> apiV1FeedgroupsIdPut(String id,
                                                                  PutFeedGroupRequest putFeedGroupRequest) {
        FeedGroupMessage feedGroupMessage = this.feedGroupService.updateFeedGroup(id,
                putFeedGroupRequest.getName(),
                putFeedGroupRequest.getDescription(),
                putFeedGroupRequest.getThumbnailUri());

        FeedGroupResponse response = new FeedGroupResponse();
        response.setData(this.feedGroupDTOFactory.valueOf(feedGroupMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * GET /api/v1/islands/{id}/feedgroups : 获得所有作品集
     *
     * @param id id (required)
     * @param page page number (optional, default to 0)
     * @param pageSize size of a page (optional, default to 10)
     * @return 用户所有作品集返回 (status code 200)
     */
    @Override
    public ResponseEntity<FeedGroupsResponse> apiV1IslandsIdFeedgroupsGet(String id,
                                                                          Integer page,
                                                                          Integer pageSize) {
        com.keepreal.madagascar.fossa.FeedGroupsResponse feedGroupsResponse =
                this.feedGroupService.retrieveFeedGroupsByIslandId(id, page, pageSize);

        FeedGroupsResponse response = new FeedGroupsResponse();
        response.setData(feedGroupsResponse.getFeedGroupsList().stream()
                .map(this.feedGroupDTOFactory::valueOf)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(feedGroupsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * POST /api/v1/islands/{id}/feedgroups : 创建一个新的作品集
     *
     * @param id id (required)
     * @param postFeedGroupRequest  (required)
     * @return 单一作品集返回 (status code 200)
     */
    @CrossOrigin
    @Override
    public ResponseEntity<FeedGroupResponse> apiV1IslandsIdFeedgroupsPost(String id,
                                                                          PostFeedGroupRequest postFeedGroupRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        FeedGroupMessage feedGroupMessage = this.feedGroupService.createFeedGroup(id,
                userId,
                postFeedGroupRequest.getName(),
                postFeedGroupRequest.getDescription(),
                postFeedGroupRequest.getThumbnailUri());

        FeedGroupResponse response = new FeedGroupResponse();
        response.setData(this.feedGroupDTOFactory.valueOf(feedGroupMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
