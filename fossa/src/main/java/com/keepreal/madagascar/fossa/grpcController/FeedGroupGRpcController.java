package com.keepreal.madagascar.fossa.grpcController;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.fossa.DeleteFeedGroupByIdRequest;
import com.keepreal.madagascar.fossa.FeedGroupFeedsResponse;
import com.keepreal.madagascar.fossa.FeedGroupResponse;
import com.keepreal.madagascar.fossa.FeedGroupServiceGrpc;
import com.keepreal.madagascar.fossa.FeedGroupsResponse;
import com.keepreal.madagascar.fossa.NewFeedGroupRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedGroupByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedGroupContentByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedGroupsByIslandIdRequest;
import com.keepreal.madagascar.fossa.UpdateFeedGroupByIdRequest;
import com.keepreal.madagascar.fossa.model.FeedGroup;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.service.FeedGroupService;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Objects;
import java.util.stream.Collectors;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * Represents the feed group rpc service.
 */
@GRpcService
public class FeedGroupGRpcController extends FeedGroupServiceGrpc.FeedGroupServiceImplBase {

    private final FeedGroupService feedGroupService;
    private final FeedInfoService feedInfoService;

    /**
     * Constructs the feed group grpc controller.
     *
     * @param feedGroupService {@link FeedGroupService}.
     * @param feedInfoService  {@link FeedInfoService}.
     */
    public FeedGroupGRpcController(FeedGroupService feedGroupService,
                                   FeedInfoService feedInfoService) {
        this.feedGroupService = feedGroupService;
        this.feedInfoService = feedInfoService;
    }

    /**
     * Implements the create feed group api.
     *
     * @param request          {@link NewFeedGroupRequest}.
     * @param responseObserver {@link FeedGroupResponse}.
     */
    @Override
    public void createFeedGroup(NewFeedGroupRequest request,
                                StreamObserver<FeedGroupResponse> responseObserver) {
        FeedGroup.FeedGroupBuilder feedGroupBuilder = FeedGroup.builder()
                .hostId(request.getUserId())
                .islandId(request.getIslandId())
                .name(request.getName());

        if (request.hasDescription()) {
            feedGroupBuilder.description(request.getDescription().getValue());
        }
        if (request.hasThumbnailUri()) {
            feedGroupBuilder.thumbnailUri(request.getThumbnailUri().getValue());
        }

        FeedGroup feedGroup = this.feedGroupService.insert(feedGroupBuilder.build());

        FeedGroupResponse response = FeedGroupResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setFeedGroup(this.feedGroupService.getFeedGroupMessage(feedGroup))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the delete feed group api.
     *
     * @param request          {@link DeleteFeedGroupByIdRequest}.
     * @param responseObserver {@link CommonStatus}.
     */
    @Override
    public void deleteFeedGroupById(DeleteFeedGroupByIdRequest request,
                                    StreamObserver<CommonStatus> responseObserver) {
        this.feedGroupService.deleteById(request.getId());

        responseObserver.onNext(CommonStatusUtils.getSuccStatus());
        responseObserver.onCompleted();
    }

    /**
     * Implements the update feed group api.
     *
     * @param request          {@link UpdateFeedGroupByIdRequest}.
     * @param responseObserver {@link FeedGroupResponse}.
     */
    @Override
    public void updateFeedGroupById(UpdateFeedGroupByIdRequest request,
                                    StreamObserver<FeedGroupResponse> responseObserver) {
        FeedGroup feedGroup = this.feedGroupService.retrieveFeedGroupById(request.getId());

        if (Objects.isNull(feedGroup)) {
            FeedGroupResponse response = FeedGroupResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEEDGROUP_NOT_FOUND_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        if (request.hasName()) {
            feedGroup.setName(request.getName().getValue());
        }

        if (request.hasDescription()) {
            feedGroup.setDescription(request.getDescription().getValue());
        }

        if (request.hasThumbnailUri()) {
            feedGroup.setThumbnailUri(request.getThumbnailUri().getValue());
        }

        feedGroup = this.feedGroupService.updateFeedGroup(feedGroup);

        FeedGroupResponse response = FeedGroupResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setFeedGroup(this.feedGroupService.getFeedGroupMessage(feedGroup))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements get feed groups by island id.
     *
     * @param request          {@link RetrieveFeedGroupsByIslandIdRequest}.
     * @param responseObserver {@link FeedGroupsResponse}.
     */
    @Override
    public void retrieveFeedGroupsByIslandId(RetrieveFeedGroupsByIslandIdRequest request,
                                             StreamObserver<FeedGroupsResponse> responseObserver) {
        Page<FeedGroup> feedGroupPage = this.feedGroupService.retrieveFeedGroupsByIslandId(request.getIslandId(),
                PageRequest.of(request.getPageRequest().getPage(), request.getPageRequest().getPageSize()));

        FeedGroupsResponse response = FeedGroupsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllFeedGroups(feedGroupPage.getContent().stream()
                        .map(this.feedGroupService::getFeedGroupMessage)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .setPageResponse(PageRequestResponseUtils.buildPageResponse(feedGroupPage))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the get feeds by feed group id.
     *
     * @param request          {@link RetrieveFeedGroupContentByIdRequest}.
     * @param responseObserver {@link FeedGroupFeedsResponse}.
     */
    @Override
    public void retrieveFeedGroupFeedsById(RetrieveFeedGroupContentByIdRequest request,
                                           StreamObserver<FeedGroupFeedsResponse> responseObserver) {
        FeedGroup feedGroup = this.feedGroupService.retrieveFeedGroupById(request.getId());

        if (Objects.isNull(feedGroup)) {
            FeedGroupFeedsResponse response = FeedGroupFeedsResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEEDGROUP_NOT_FOUND_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        Page<FeedInfo> feedInfoPage = this.feedInfoService.retrieveFeedsByFeedGroupId(request.getId(),
                PageRequest.of(request.getPageRequest().getPage(), request.getPageRequest().getPageSize()));

        FeedGroupFeedsResponse feedsResponse = FeedGroupFeedsResponse.newBuilder()
                .addAllFeed(feedInfoPage.getContent().stream()
                        .map(feed -> this.feedInfoService.getFeedMessage(feed, request.getUserId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .setPageResponse(PageRequestResponseUtils.buildPageResponse(feedInfoPage))
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(feedsResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the get feed group by id.
     *
     * @param request           {@link RetrieveFeedGroupByIdRequest}.
     * @param responseObserver  {@link FeedGroupResponse}.
     */
    @Override
    public void retrieveFeedGroupById(RetrieveFeedGroupByIdRequest request,
                                      StreamObserver<FeedGroupResponse> responseObserver) {
        FeedGroup feedGroup = this.feedGroupService.retrieveFeedGroupById(request.getId());
        FeedGroupResponse response;
        if (Objects.isNull(feedGroup)) {
            response = FeedGroupResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEEDGROUP_NOT_FOUND_ERROR))
                    .build();
        } else {
            response = FeedGroupResponse.newBuilder()
                    .setStatus(CommonStatusUtils.getSuccStatus())
                    .setFeedGroup(this.feedGroupService.getFeedGroupMessage(feedGroup))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
