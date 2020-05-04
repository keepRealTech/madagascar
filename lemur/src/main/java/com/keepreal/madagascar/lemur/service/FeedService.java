package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.CheckNewFeedsMessage;
import com.keepreal.madagascar.fossa.CheckNewFeedsRequest;
import com.keepreal.madagascar.fossa.CheckNewFeedsResponse;
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
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Represents the feed service.
 */
@Service
@Slf4j
public class FeedService {

    private final ManagedChannel managedChannel;

    /**
     * Constructs the feed service.
     *
     * @param managedChannel GRpc managed channel connection to service Fossa.
     */
    public FeedService(@Qualifier("fossaChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
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
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.managedChannel);

        NewFeedsRequest request = NewFeedsRequest.newBuilder()
                .addAllIslandId(islandIds)
                .setUserId(userId)
                .setText(StringValue.of(text))
                .addAllImageUris(imageUris)
                .build();

        NewFeedsResponse newFeedsResponse;
        try {
            newFeedsResponse = stub.createFeeds(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.managedChannel);

        DeleteFeedByIdRequest request = DeleteFeedByIdRequest.newBuilder()
                .setId(id)
                .build();

        DeleteFeedResponse deleteFeedResponse;
        try {
            deleteFeedResponse = stub.deleteFeedById(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
    public FeedMessage retrieveFeedById(String id) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.managedChannel);

        RetrieveFeedByIdRequest request = RetrieveFeedByIdRequest.newBuilder()
                .setId(id)
                .build();

        FeedResponse feedResponse;
        try {
            feedResponse = stub.retrieveFeedById(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
    public FeedsResponse retrieveFeeds(String islandId, Boolean fromHost, int page, int pageSize) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.managedChannel);

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
                .build();

        FeedsResponse feedsResponse;
        try {
            feedsResponse = stub.retrieveMultipleFeeds(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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

    /**
     * Checks if has new feeds after the given timestamp.
     *
     * @param islandIds Island ids.
     * @param timestamps Timestamps in milli-seconds.
     * @return List of {@link CheckNewFeedsMessage}.
     */
    public List<CheckNewFeedsMessage> checkNewFeeds(List<String> islandIds, List<Long> timestamps) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.managedChannel);

        CheckNewFeedsRequest request = CheckNewFeedsRequest.newBuilder()
                .addAllIslandIds(islandIds)
                .addAllTimestamps(timestamps)
                .build();

        CheckNewFeedsResponse checkNewFeedsResponse;
        try {
            checkNewFeedsResponse = stub.checkNewFeeds(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
        }

        if (Objects.isNull(checkNewFeedsResponse)
                || !checkNewFeedsResponse.hasStatus()) {
            log.error(Objects.isNull(checkNewFeedsResponse) ? "Retrieve feed returned null." : checkNewFeedsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != checkNewFeedsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(checkNewFeedsResponse.getStatus());
        }

        return checkNewFeedsResponse.getCheckNewFeedsList();
    }

}
