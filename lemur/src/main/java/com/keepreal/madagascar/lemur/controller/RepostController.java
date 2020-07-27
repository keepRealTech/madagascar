package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.fossa.FeedRepostMessage;
import com.keepreal.madagascar.fossa.FeedRepostsResponse;
import com.keepreal.madagascar.fossa.IslandRepostMessage;
import com.keepreal.madagascar.fossa.IslandRepostsResponse;
import com.keepreal.madagascar.lemur.dtoFactory.RepostDTOFactory;
import com.keepreal.madagascar.lemur.service.RepostService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.RepostApi;
import swagger.model.PostRepostRequest;
import swagger.model.RepostResponse;
import swagger.model.RepostsResponse;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the repost controller.
 */
@RestController
public class RepostController implements RepostApi {

    private final RepostService repostService;
    private final RepostDTOFactory repostDTOFactory;

    /**
     * Constructs the repost controller.
     *
     * @param repostService     {@link RepostService}.
     * @param repostDTOFactory  {@link RepostDTOFactory}.
     */
    public RepostController(RepostService repostService,
                            RepostDTOFactory repostDTOFactory) {
        this.repostService = repostService;
        this.repostDTOFactory = repostDTOFactory;
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
     * Implements the get reposts by island id api.
     *
     * @param id       Island id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link RepostsResponse}.
     */
    @Override
    public ResponseEntity<RepostsResponse> apiV1IslandsIdRepostsGet(String id,
                                                                    Integer page,
                                                                    Integer pageSize) {
        IslandRepostsResponse islandRepostsResponse =
                this.repostService.retrieveRepostIslandById(id, page, pageSize);

        RepostsResponse response = new RepostsResponse();
        response.setData(islandRepostsResponse.getIslandRepostsList()
                .stream()
                .map(this.repostDTOFactory::valueOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(islandRepostsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the repost island api.
     *
     * @param id                Island id.
     * @param postRepostRequest {@link PostRepostRequest}.
     * @return {@link RepostResponse}.
     */
    @Override
    public ResponseEntity<RepostResponse> apiV1IslandsIdRepostsPost(String id,
                                                                    PostRepostRequest postRepostRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        IslandRepostMessage repostMessage = this.repostService.createRepostIslandById(
                id, userId, postRepostRequest.getContent(), postRepostRequest.getIsSuccessful());

        RepostResponse response = new RepostResponse();
        response.setData(this.repostDTOFactory.valueOf(repostMessage));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
