package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int64Value;
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
import com.keepreal.madagascar.fossa.RetrieveFeedsByIdsRequest;
import com.keepreal.madagascar.fossa.RetrieveMultipleFeedsRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.mantella.RetrieveMultipleTimelinesRequest;
import com.keepreal.madagascar.mantella.TimelineMessage;
import com.keepreal.madagascar.mantella.TimelineServiceGrpc;
import com.keepreal.madagascar.mantella.TimelinesResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the feed service.
 */
@Service
@Slf4j
public class FeedService {

    private final Channel fossaChannel;
    private final Channel mantellaChannel;
    private final IslandService islandService;

    /**
     * Constructs the feed service.
     *
     * @param fossaChannel    GRpc managed channel connection to service Fossa.
     * @param mantellaChannel
     * @param islandService   {@link IslandService}
     */

    public FeedService(@Qualifier("fossaChannel") Channel fossaChannel,
                       @Qualifier("mantellaChannel") Channel mantellaChannel,
                       IslandService islandService) {
        this.fossaChannel = fossaChannel;
        this.mantellaChannel = mantellaChannel;
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
    public void createFeed(List<String> islandIds, List<String> membershipIds, String userId, String text, List<String> imageUris) {
        if (Objects.isNull(islandIds) || islandIds.size() == 0) {
            log.error("param islandIds is invalid");
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        List<String> hostIdList = islandIds.stream().map(id -> islandService.retrieveIslandById(id).getHostId()).collect(Collectors.toList());

        NewFeedsRequest request = NewFeedsRequest.newBuilder()
                .addAllIslandId(islandIds)
                .addAllHostId(hostIdList)
                .setUserId(userId)
                .setText(StringValue.of(text))
                .addAllImageUris(imageUris)
                .addAllMembershipIds(membershipIds)
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
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

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
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

        RetrieveFeedByIdRequest request = RetrieveFeedByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .setIncludeDeleted(false)
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
     * Retrieves a feed by id.
     *
     * @param id Feed id.
     * @return True if deleted.
     */
    public boolean checkDeleted(String id) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

        RetrieveFeedByIdRequest request = RetrieveFeedByIdRequest.newBuilder()
                .setId(id)
                .setIncludeDeleted(true)
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

        return feedResponse.getFeed().getIsDeleted();
    }

    /**
     * Retrieves feeds.
     *
     * @param islandId       Island id.
     * @param fromHost       Whether filter by from host.
     * @param userId         User id.
     * @param timestampAfter Timestamp after.
     * @param page           Page index.
     * @param pageSize       Page size.
     * @return {@link FeedsResponse}.
     */
    public FeedsResponse retrieveIslandFeeds(String islandId, Boolean fromHost, String userId, Long timestampAfter, int page, int pageSize) {
        Assert.hasText(islandId, "Island id is null.");

        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

        QueryFeedCondition.Builder conditionBuilder = QueryFeedCondition.newBuilder();
        conditionBuilder.setIslandId(StringValue.of(islandId));

        if (Objects.nonNull(fromHost)) {
            conditionBuilder.setFromHost(BoolValue.of(fromHost));
        }

        if (Objects.nonNull(timestampAfter)) {
            conditionBuilder.setTimestampAfter(Int64Value.of(timestampAfter));
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

    /**
     * Retrieves feeds.
     *
     * @param userId         User id.
     * @param timestampAfter Timestamp filter.
     * @param pageSize       Page size.
     * @return {@link FeedsResponse}.
     */
    public FeedsResponse retrieveUserFeeds(String userId, long timestampAfter, int pageSize) {
        Assert.hasText(userId, "User id is null.");

        TimelinesResponse timelinesResponse = this.retrieveTimelinesByUserId(userId, timestampAfter, pageSize);

        return this.retrieveFeedsByIds(timelinesResponse.getTimelinesList()
                .stream()
                .map(TimelineMessage::getFeedId)
                .collect(Collectors.toList()), userId);
    }

    /**
     * Retrieves timelines by user id.
     *
     * @param userId         User id.
     * @param timestampAfter Timestamp after.
     * @param pageSize       Page size.
     * @return {@link TimelinesResponse}.
     */
    private TimelinesResponse retrieveTimelinesByUserId(String userId, long timestampAfter, int pageSize) {
        TimelineServiceGrpc.TimelineServiceBlockingStub mantellaStub = TimelineServiceGrpc.newBlockingStub(this.mantellaChannel);

        RetrieveMultipleTimelinesRequest timelinesRequest = RetrieveMultipleTimelinesRequest.newBuilder()
                .setCreatedAfter(timestampAfter)
                .setUserId(userId)
                .setPageRequest(PaginationUtils.buildPageRequest(0, pageSize))
                .build();

        TimelinesResponse timelinesResponse;
        try {
            timelinesResponse = mantellaStub.retrieveMultipleTimelines(timelinesRequest);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(timelinesResponse)
                || !timelinesResponse.hasStatus()) {
            log.error(Objects.isNull(timelinesResponse) ? "Retrieve timelines returned null." : timelinesResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != timelinesResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(timelinesResponse.getStatus());
        }

        return timelinesResponse;
    }

    /**
     * Retrieves feeds by ids.
     *
     * @param feedIds Feed ids.
     * @return {@link FeedsResponse}.
     */
    private FeedsResponse retrieveFeedsByIds(List<String> feedIds, String userId) {
        if (feedIds.isEmpty()) {
            return FeedsResponse.newBuilder().build();
        }

        FeedServiceGrpc.FeedServiceBlockingStub fossaStub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        RetrieveFeedsByIdsRequest byIdsRequest = RetrieveFeedsByIdsRequest.newBuilder()
                .addAllIds(feedIds)
                .setUserId(userId)
                .build();

        FeedsResponse feedsResponse;
        try {
            feedsResponse = fossaStub.retrieveFeedsByIds(byIdsRequest);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(feedsResponse)
                || !feedsResponse.hasStatus()) {
            log.error(Objects.isNull(feedsResponse) ? "Retrieve timelines returned null." : feedsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != feedsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(feedsResponse.getStatus());
        }

        return feedsResponse;
    }

}
