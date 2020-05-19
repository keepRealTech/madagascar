package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.DeleteFeedByIdRequest;
import com.keepreal.madagascar.fossa.DeleteFeedResponse;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.fossa.NewFeedsRequest;
import com.keepreal.madagascar.fossa.NewFeedsResponse;
import com.keepreal.madagascar.fossa.QueryFeedCondition;
import com.keepreal.madagascar.fossa.RetrieveFeedByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveMultipleFeedsRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the feed service.
 */
@Service
@Slf4j
public class FeedService {

    private final Channel channel;
    private final IslandService islandService;

    /**
     * Constructs the feed service.
     *
     * @param channel       GRpc managed channel connection to service Fossa.
     * @param islandService {@link IslandService}
     */

    public FeedService(@Qualifier("fossaChannel") Channel channel,
                       IslandService islandService) {
        this.channel = channel;
        this.islandService = islandService;
    }

    /**
     * Creates a new feed.
     *
     * @param islandIds Island ids.
     * @param userId    User id.
     * @param text      Text content.
     * @param imageUris Image uris.
     */
    public void createFeed(List<String> islandIds, String userId, String text, List<String> imageUris) {
        if (Objects.isNull(islandIds) || islandIds.size() == 0) {
            log.error("param islandIds is invalid");
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.channel);
        List<String> hostIdList = islandIds.stream().map(id -> islandService.retrieveIslandById(id).getHostId()).collect(Collectors.toList());

        NewFeedsRequest request = NewFeedsRequest.newBuilder()
                .addAllIslandId(islandIds)
                .addAllHostId(hostIdList)
                .setUserId(userId)
                .setText(StringValue.of(text))
                .addAllImageUris(imageUris)
                .build();

        NewFeedsResponse newFeedsResponse;
        try {
            newFeedsResponse = stub.createFeeds(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(newFeedsResponse)
                || !newFeedsResponse.hasStatus()) {
            log.error(Objects.isNull(newFeedsResponse) ? "Create feed returned null." : newFeedsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != newFeedsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(newFeedsResponse.getStatus());
        }
    }

    /**
     * Deletes a feed by id.
     *
     * @param id Feed id.
     */
    public void deleteFeedById(String id) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.channel);

        DeleteFeedByIdRequest request = DeleteFeedByIdRequest.newBuilder()
                .setId(id)
                .build();

        DeleteFeedResponse deleteFeedResponse;
        try {
            deleteFeedResponse = stub.deleteFeedById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(deleteFeedResponse)
                || !deleteFeedResponse.hasStatus()) {
            log.error(Objects.isNull(deleteFeedResponse) ? "Delete feed returned null." : deleteFeedResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != deleteFeedResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(deleteFeedResponse.getStatus());
        }
    }

    /**
     * Retrieves a feed by id.
     *
     * @param id Feed id.
     * @return {@link FeedMessage}.
     */
    public FeedMessage retrieveFeedById(String id, String userId) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.channel);

        RetrieveFeedByIdRequest request = RetrieveFeedByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        FeedResponse feedResponse;
        try {
            feedResponse = stub.retrieveFeedById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(feedResponse)
                || !feedResponse.hasStatus()) {
            log.error(Objects.isNull(feedResponse) ? "Retrieve feed returned null." : feedResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != feedResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(feedResponse.getStatus());
        }

        return feedResponse.getFeed();
    }

    /**
     * Retrieves feeds.
     *
     * @param islandId Island id.
     * @param fromHost Whether filter by from host.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link FeedsResponse}.
     */
    public FeedsResponse retrieveFeeds(String islandId, Boolean fromHost, String userId, int page, int pageSize) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.channel);

        QueryFeedCondition.Builder conditionBuilder = QueryFeedCondition.newBuilder();

        if (!StringUtils.isEmpty(islandId)) {
            conditionBuilder.setIslandId(StringValue.of(islandId));
        }

        if (Objects.nonNull(fromHost)) {
            conditionBuilder.setFromHost(BoolValue.of(fromHost));
        }

        RetrieveMultipleFeedsRequest request = RetrieveMultipleFeedsRequest.newBuilder()
                .setCondition(conditionBuilder.build())
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .setUserId(userId)
                .build();

        FeedsResponse feedsResponse;
        try {
            feedsResponse = stub.retrieveMultipleFeeds(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(feedsResponse)
                || !feedsResponse.hasStatus()) {
            log.error(Objects.isNull(feedsResponse) ? "Retrieve feeds returned null." : feedsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != feedsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(feedsResponse.getStatus());
        }

        return feedsResponse;
    }

}
