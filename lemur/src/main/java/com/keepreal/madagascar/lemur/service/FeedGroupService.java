package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.DeleteFeedGroupByIdRequest;
import com.keepreal.madagascar.fossa.FeedGroupFeedsResponse;
import com.keepreal.madagascar.fossa.FeedGroupMessage;
import com.keepreal.madagascar.fossa.FeedGroupResponse;
import com.keepreal.madagascar.fossa.FeedGroupServiceGrpc;
import com.keepreal.madagascar.fossa.FeedGroupsResponse;
import com.keepreal.madagascar.fossa.NewFeedGroupRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedGroupContentByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedGroupsByIslandIdRequest;
import com.keepreal.madagascar.fossa.UpdateFeedGroupByIdRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the feed group service.
 */
@Service
@Slf4j
public class FeedGroupService {

    private final Channel fossaChannel;

    /**
     * Constructs the feed group service.
     *
     * @param fossaChannel GRpc managed channel connection to service Fossa.
     */
    public FeedGroupService(Channel fossaChannel) {
        this.fossaChannel = fossaChannel;
    }

    /**
     * Deletes a feed group by id.
     *
     * @param id Feed group id.
     */
    public void deleteFeedGroupById(String id) {
        FeedGroupServiceGrpc.FeedGroupServiceBlockingStub stub = FeedGroupServiceGrpc.newBlockingStub(this.fossaChannel);

        DeleteFeedGroupByIdRequest request = DeleteFeedGroupByIdRequest.newBuilder()
                .setId(id)
                .build();

        CommonStatus response;
        try {
            response = stub.deleteFeedGroupById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)) {
            log.error("Delete feed group returned null.");
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getRtn()) {
            throw new KeepRealBusinessException(response);
        }
    }

    /**
     * Creates a new feed group.
     *
     * @param islandId     Island id.
     * @param hostId       Host id.
     * @param name         Name.
     * @param description  Description.
     * @param thumbnailUri Thumbnail uri.
     * @return {@link FeedGroupMessage}.
     */
    public FeedGroupMessage createFeedGroup(String islandId, String hostId, String name, String description, String thumbnailUri) {
        FeedGroupServiceGrpc.FeedGroupServiceBlockingStub stub = FeedGroupServiceGrpc.newBlockingStub(this.fossaChannel);

        NewFeedGroupRequest.Builder requestBuilder = NewFeedGroupRequest.newBuilder()
                .setIslandId(islandId)
                .setUserId(hostId)
                .setName(name.trim());

        if (!Objects.isNull(description)) {
            requestBuilder.setDescription(StringValue.of(description.trim()));
        }

        if (!Objects.isNull(thumbnailUri)) {
            requestBuilder.setDescription(StringValue.of(thumbnailUri.trim()));
        }

        FeedGroupResponse response;
        try {
            response = stub.createFeedGroup(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Create feed group returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getFeedGroup();
    }

    /**
     * Updates a feed group by id.
     *
     * @param id           Feed group id.
     * @param name         New name.
     * @param description  Description.
     * @param thumbnailUri Thumbnail uri.
     * @return Updated @link FeedGroupMessage}.
     */
    public FeedGroupMessage updateFeedGroup(String id, String name, String description, String thumbnailUri) {
        FeedGroupServiceGrpc.FeedGroupServiceBlockingStub stub = FeedGroupServiceGrpc.newBlockingStub(this.fossaChannel);

        UpdateFeedGroupByIdRequest.Builder requestBuilder = UpdateFeedGroupByIdRequest.newBuilder()
                .setId(id);

        if (!Objects.isNull(name)) {
            requestBuilder.setName(StringValue.of(name.trim()));
        }

        if (!Objects.isNull(description)) {
            requestBuilder.setDescription(StringValue.of(description.trim()));
        }

        if (!Objects.isNull(thumbnailUri)) {
            requestBuilder.setDescription(StringValue.of(thumbnailUri.trim()));
        }

        FeedGroupResponse response;
        try {
            response = stub.updateFeedGroupById(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Update feed group returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getFeedGroup();
    }

    /**
     * Retrieves the feed groups by island id.
     *
     * @param islandId Island id.
     * @param page     Page.
     * @param pageSize Page size.
     * @return {@link FeedGroupsResponse}.
     */
    public FeedGroupsResponse retrieveFeedGroupsByIslandId(String islandId, int page, int pageSize) {
        FeedGroupServiceGrpc.FeedGroupServiceBlockingStub stub = FeedGroupServiceGrpc.newBlockingStub(this.fossaChannel);

        RetrieveFeedGroupsByIslandIdRequest request = RetrieveFeedGroupsByIslandIdRequest.newBuilder()
                .setIslandId(islandId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        FeedGroupsResponse response;
        try {
            response = stub.retrieveFeedGroupsByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve feed group feeds returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Retrieves the feeds of a feed group.
     *
     * @param id       Feed group id.
     * @param userId   User id.
     * @param page     Page.
     * @param pageSize Page size.
     * @return {@link FeedGroupFeedsResponse}.
     */
    public FeedGroupFeedsResponse retrieveFeedGroupFeeds(String id, String userId, int page, int pageSize) {
        FeedGroupServiceGrpc.FeedGroupServiceBlockingStub stub = FeedGroupServiceGrpc.newBlockingStub(this.fossaChannel);

        RetrieveFeedGroupContentByIdRequest request = RetrieveFeedGroupContentByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        FeedGroupFeedsResponse response;
        try {
            response = stub.retrieveFeedGroupFeedsById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve feed group feeds returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

}
