package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt64Value;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.VideoMessage;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.DeleteFeedByIdRequest;
import com.keepreal.madagascar.fossa.DeleteFeedResponse;
import com.keepreal.madagascar.fossa.FeedGroupFeedResponse;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.fossa.NewFeedsRequest;
import com.keepreal.madagascar.fossa.NewFeedsRequestV2;
import com.keepreal.madagascar.fossa.NewFeedsResponse;
import com.keepreal.madagascar.fossa.QueryFeedCondition;
import com.keepreal.madagascar.fossa.RetrieveFeedByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedCountRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedCountResponse;
import com.keepreal.madagascar.fossa.RetrieveFeedsByIdsRequest;
import com.keepreal.madagascar.fossa.RetrieveMembershipFeedsRequest;
import com.keepreal.madagascar.fossa.RetrieveMultipleFeedsRequest;
import com.keepreal.madagascar.fossa.RetrieveToppedFeedByIdRequest;
import com.keepreal.madagascar.fossa.TopFeedByIdRequest;
import com.keepreal.madagascar.fossa.TopFeedByIdResponse;
import com.keepreal.madagascar.fossa.UpdateFeedByVideoRequest;
import com.keepreal.madagascar.fossa.UpdateFeedByVideoResponse;
import com.keepreal.madagascar.fossa.UpdateFeedFeedgroupRequest;
import com.keepreal.madagascar.fossa.UpdateFeedRequest;
import com.keepreal.madagascar.fossa.UpdateFeedSaveAuthorityRequest;
import com.keepreal.madagascar.fossa.UpdateFeedSaveAuthorityResponse;
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
import org.springframework.util.StringUtils;
import swagger.model.MultiMediaDTO;
import swagger.model.TopFeedRequest;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
    private final MediaService mediaService;

    /**
     * Constructs the feed service.
     *
     * @param fossaChannel    GRpc managed channel connection to service Fossa.
     * @param mantellaChannel GRpc managed channel connection to service Mantella.
     * @param islandService   {@link IslandService}.
     * @param mediaService    {@link MediaService}.
     */
    public FeedService(@Qualifier("fossaChannel") Channel fossaChannel,
                       @Qualifier("mantellaChannel") Channel mantellaChannel,
                       IslandService islandService,
                       MediaService mediaService) {
        this.fossaChannel = fossaChannel;
        this.mantellaChannel = mantellaChannel;
        this.islandService = islandService;
        this.mediaService = mediaService;
    }

    /**
     * Creates a new feed.
     *
     * @param islandIds Island ids.
     * @param userId    User id.
     * @param text      Text content.
     * @param imageUris Image uris.
     */
    public void createFeeds(List<String> islandIds, List<String> membershipIds, String userId, String text, List<String> imageUris) {
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
                .addAllMembershipIds(membershipIds == null ? Collections.emptyList() : membershipIds)
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

    public void createFeedsV2(List<String> islandIds,
                              List<String> membershipIds,
                              String userId,
                              MediaType mediaType,
                              List<MultiMediaDTO> multiMediaDTOList,
                              String text,
                              String feedGroupId,
                              Long priceInCents,
                              Boolean isWorks) {
        if (Objects.isNull(islandIds) || islandIds.size() == 0) {
            log.error("param islandIds is invalid");
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        }

        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        List<String> hostIdList = islandIds.stream().map(id -> islandService.retrieveIslandById(id).getHostId()).collect(Collectors.toList());

        NewFeedsRequestV2.Builder builder = NewFeedsRequestV2.newBuilder()
                .addAllIslandId(islandIds)
                .addAllHostId(hostIdList)
                .addAllMembershipIds(Objects.isNull(membershipIds) ? new ArrayList<>() : membershipIds)
                .setUserId(userId)
                .setType(mediaType);

        if (!Objects.isNull(feedGroupId)) {
            builder.setFeedGroupId(StringValue.of(feedGroupId));
        }

        if (priceInCents != null && priceInCents > 0) {
            builder.setPriceInCents(Int64Value.of(priceInCents));
        }

        if (isWorks != null) {
            builder.setIsWorks(BoolValue.of(isWorks));
        }

        this.buildMediaMessage(builder, mediaType, multiMediaDTOList, text);

        NewFeedsResponse newFeedsResponse;
        try {
            newFeedsResponse = stub.createFeedsV2(builder.build());
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
     * Creates a feed in a given island.
     *
     * @param islandId          Island id.
     * @param membershipIds     Membership ids.
     * @param userId            Creator id.
     * @param mediaType         Media type.
     * @param multiMediaDTOList Media entity.
     * @param text              Text.
     * @param title             Title.
     * @param brief             Brief.
     * @param feedGroupId       Feed group id.
     * @param priceInCents      Price in cents.
     * @return {@link FeedMessage}.
     */
    public FeedMessage createFeed(String islandId,
                                  List<String> membershipIds,
                                  String userId,
                                  MediaType mediaType,
                                  List<MultiMediaDTO> multiMediaDTOList,
                                  String text,
                                  String title,
                                  String brief,
                                  String feedGroupId,
                                  Long priceInCents,
                                  Boolean isWorks) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        String hostId = this.islandService.retrieveIslandById(islandId).getHostId();

        NewFeedsRequestV2.Builder builder = NewFeedsRequestV2.newBuilder()
                .addIslandId(islandId)
                .addHostId(hostId)
                .addAllMembershipIds(Objects.isNull(membershipIds) ? new ArrayList<>() : membershipIds)
                .setUserId(userId)
                .setType(mediaType);

        if (Objects.nonNull(feedGroupId)) {
            builder.setFeedGroupId(StringValue.of(feedGroupId));
        }

        if (priceInCents != null && priceInCents > 0) {
            builder.setPriceInCents(Int64Value.of(priceInCents));
        }

        if (Objects.nonNull(title)) {
            builder.setTitle(StringValue.of(title));
        }

        if (Objects.nonNull(brief)) {
            builder.setBrief(StringValue.of(brief));
        }

        if (Objects.nonNull(isWorks)) {
            builder.setIsWorks(BoolValue.of(isWorks));
        }

        this.buildMediaMessage(builder, mediaType, multiMediaDTOList, text, true);

        FeedResponse feedResponse;
        try {
            feedResponse = stub.createFeed(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(feedResponse)
                || !feedResponse.hasStatus()) {
            log.error(Objects.isNull(feedResponse) ? "Create feed returned null." : feedResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != feedResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(feedResponse.getStatus());
        }

        return feedResponse.getFeed();
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
     * @param id     Feed id.
     * @param userId User id.
     * @return {@link FeedGroupFeedResponse}.
     */
    public FeedGroupFeedResponse retrieveFeedGroupFeedById(String id, String userId) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

        RetrieveFeedByIdRequest request = RetrieveFeedByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .setIncludeDeleted(false)
                .build();

        FeedGroupFeedResponse response;
        try {
            response = stub.retrieveFeedGroupFeedById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve feed returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Updates a feed's feed group by id.
     *
     * @param id          Feed id.
     * @param userId      User id.
     * @param feedgroupId Feed group id.
     * @return {@link FeedGroupFeedResponse}.
     */
    public FeedGroupFeedResponse updateFeedFeedgroupById(String id, String userId, String feedgroupId, Boolean isRemove) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

        UpdateFeedFeedgroupRequest request = UpdateFeedFeedgroupRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .setFeedgroupId(feedgroupId)
                .setIsRemove(isRemove == null ? false : isRemove)
                .build();

        FeedGroupFeedResponse response;
        try {
            response = stub.updateFeedFeedgroupById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Update feed returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Retrieves a feed with feed group info by id.
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
    public FeedsResponse retrieveIslandFeeds(String islandId, Boolean fromHost, String userId, Long timestampAfter,
                                             Long timestampBefore, int page, int pageSize, Boolean excludeTopped) {
        return retrieveIslandFeeds(islandId, fromHost, userId, timestampAfter, timestampBefore, page, pageSize, excludeTopped, null);
    }

    public FeedsResponse retrieveIslandFeeds(String islandId, Boolean fromHost, String userId, Long timestampAfter,
                                             Long timestampBefore, int page, int pageSize, Boolean excludeTopped, Boolean isWorks) {
        Assert.hasText(islandId, "Island id is null.");

        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

        QueryFeedCondition.Builder conditionBuilder = QueryFeedCondition.newBuilder();
        conditionBuilder.setIslandId(StringValue.of(islandId));

        if (Objects.nonNull(fromHost)) {
            conditionBuilder.setFromHost(BoolValue.of(fromHost));
        }

        if (Objects.nonNull(timestampBefore)) {
            conditionBuilder.setTimestampBefore(Int64Value.of(timestampBefore));
        } else {
            conditionBuilder.setTimestampAfter(Int64Value.of(timestampAfter == null ? 0L : timestampAfter));
        }

        if (Objects.nonNull(excludeTopped)) {
            conditionBuilder.setExcludeTopped(BoolValue.of(excludeTopped));
        }

        if (Objects.nonNull(isWorks)) {
            conditionBuilder.setIsWorks(BoolValue.of(isWorks));
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

    public FeedsResponse retrieveIslandMembershipFeeds(String userId,
                                                       String islandId,
                                                       Long timestampAfter,
                                                       Long timestampBefore,
                                                       int pageSize,
                                                       List<String> feedIdList,
                                                       List<String> membershipIdList) {
        FeedServiceGrpc.FeedServiceBlockingStub fossaStub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

        RetrieveMembershipFeedsRequest.Builder builder = RetrieveMembershipFeedsRequest.newBuilder()
                .setPageRequest(PaginationUtils.buildPageRequest(0, pageSize))
                .setUserId(userId)
                .setIslandId(islandId)
                .addAllFeedIds(feedIdList)
                .addAllMembershipIds(membershipIdList);

        if (Objects.nonNull(timestampBefore)) {
            builder.setTimestampBefore(Int64Value.of(timestampBefore));
        } else {
            builder.setTimestampAfter(Int64Value.of(timestampAfter == null ? 0L : timestampAfter));
        }

        FeedsResponse response;

        try {
            response = fossaStub.retrieveMembershipFeeds(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve membership feeds returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Retrieves feeds.
     *
     * @param userId          User id.
     * @param timestampAfter  Timestamp filter.
     * @param timestampBefore Timestamp before.
     * @param pageSize        Page size.
     * @return {@link FeedsResponse}.
     */
    public AbstractMap.SimpleEntry<Boolean, FeedsResponse> retrieveUserFeeds(String userId, Long timestampAfter, Long timestampBefore, int pageSize) {
        Assert.hasText(userId, "User id is null.");

        TimelinesResponse timelinesResponse = this.retrieveTimelinesByUserId(userId, timestampAfter, timestampBefore, pageSize);

        return new AbstractMap.SimpleEntry<>(timelinesResponse.getHasMore(),
                this.retrieveFeedsByIds(timelinesResponse.getTimelinesList()
                        .stream()
                        .map(TimelineMessage::getFeedId)
                        .collect(Collectors.toList()), userId));
    }

    /**
     * Retrieves public feeds.
     *
     * @param userId          User id.
     * @param timestampAfter  Timestamp filter.
     * @param timestampBefore Timestamp before.
     * @param pageSize        Page size.
     * @return {@link FeedMessage}.
     */
    public AbstractMap.SimpleEntry<Boolean, List<AbstractMap.SimpleEntry<Long, FeedMessage>>> retrievePublicFeeds(String userId, Long timestampAfter, Long timestampBefore, int pageSize) {
        TimelinesResponse timelinesResponse = this.retrieveTimelinesByUserId(Constants.PUBLIC_INBOX_USER_ID,
                timestampAfter,
                timestampBefore,
                pageSize);

        FeedsResponse feedsResponse = this.retrieveFeedsByIds(timelinesResponse.getTimelinesList()
                .stream()
                .map(TimelineMessage::getFeedId)
                .collect(Collectors.toList()), userId);
        Map<String, FeedMessage> feedMap = feedsResponse.getFeedList().stream()
                .collect(Collectors.toMap(FeedMessage::getId, Function.identity()));

        return new AbstractMap.SimpleEntry<>(timelinesResponse.getHasMore(),
                timelinesResponse.getTimelinesList().stream()
                        .map(timelineMessage -> {
                            FeedMessage feedMessage = feedMap.get(timelineMessage.getFeedId());
                            return new AbstractMap.SimpleEntry<>(
                                    timelineMessage.getRecommendatedAt(),
                                    feedMessage
                            );
                        })
                        .collect(Collectors.toList()));
    }

    /**
     * Retrieves feeds by ids.
     *
     * @param feedIds Feed ids.
     * @return {@link FeedsResponse}.
     */
    public FeedsResponse retrieveFeedsByIds(Collection<String> feedIds, String userId) {
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

    /**
     * top or cancel top feed by request
     *
     * @param topFeedRequest request
     * @param id             island id
     */
    public void topFeedByRequest(TopFeedRequest topFeedRequest, String id) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        TopFeedByIdRequest request = TopFeedByIdRequest.newBuilder()
                .setId(topFeedRequest.getFeedId())
                .setIsRevoke(topFeedRequest.getIsRevoke())
                .setIslandId(id)
                .build();
        TopFeedByIdResponse response;
        try {
            response = stub.topFeedById(request);
        } catch (StatusRuntimeException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, e.getMessage());
        }

        if (Objects.isNull(response) || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "top feed returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }

    /**
     * retrieve topped feed (only one in this version) of the island by island id
     *
     * @param islandId island id
     * @return feed response
     */
    public FeedResponse retrieveIslandToppedFeeds(String islandId, String userId) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        RetrieveToppedFeedByIdRequest request = RetrieveToppedFeedByIdRequest.newBuilder()
                .setIslandId(islandId)
                .setUserId(userId)
                .build();

        FeedResponse response;
        try {
            response = stub.retrieveToppedFeedById(request);
        } catch (StatusRuntimeException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, e.getMessage());
        }

        if (Objects.isNull(response) || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve topped feed returns null" : response.toString());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Retrieves the feed count for an island.
     *
     * @param islandId Island id.
     * @return Count.
     */
    public Integer retrieveFeedCountByIslandId(String islandId) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        RetrieveFeedCountRequest request = RetrieveFeedCountRequest.newBuilder()
                .setIslandId(islandId)
                .build();

        RetrieveFeedCountResponse response;
        try {
            response = stub.retrieveFeedCountByIslandId(request);
        } catch (StatusRuntimeException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, e.getMessage());
        }

        if (Objects.isNull(response) || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve island feed count returns null" : response.toString());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getFeedCount();
    }

    public void updateFeedSaveAuthority(String feedId, boolean canSave) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

        UpdateFeedSaveAuthorityResponse response;
        try {
            response = stub.updateFeedSaveAuthority(UpdateFeedSaveAuthorityRequest.newBuilder()
                    .setFeedId(feedId)
                    .setCanSave(canSave)
                    .build());
        } catch (StatusRuntimeException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, e.getMessage());
        }

        if (Objects.isNull(response) || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "update feed save authority returns null" : response.toString());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }

    /**
     * Updates a feed by id
     *
     * @param id    feed id
     * @param title title
     * @param text  text
     * @param brief brief
     * @return {@link FeedMessage}
     */
    public FeedMessage updateFeedById(String id,
                                      String title,
                                      String text,
                                      String brief) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);
        UpdateFeedRequest.Builder builder = UpdateFeedRequest.newBuilder().setId(id);

        if (!StringUtils.isEmpty(title)) {
            builder.setTitle(StringValue.of(title));
        }

        if (!StringUtils.isEmpty(text)) {
            builder.setText(StringValue.of(text));
        }

        if (!StringUtils.isEmpty(brief)) {
            builder.setBrief(StringValue.of(brief));
        }

        FeedResponse response;

        try {
            response = stub.updateFeed(builder.build());
        } catch (StatusRuntimeException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, e.getMessage());
        }

        if (Objects.isNull(response) || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "update feed returns null" : response.toString());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getFeed();
    }

    public FeedMessage updateFeedByVideoId(String videoId, VideoMessage videoMessage) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.fossaChannel);

        UpdateFeedByVideoRequest request = UpdateFeedByVideoRequest.newBuilder()
                .setVideoId(videoId)
                .setMessage(videoMessage)
                .build();

        UpdateFeedByVideoResponse response;
        try {
            response = stub.updateFeedByVideoId(request);
        } catch (StatusRuntimeException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, e.getMessage());
        }

        if (Objects.isNull(response) || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "update feed by video returns null" : response.toString());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getFeed();
    }

    /**
     * Retrieves timelines by user id.
     *
     * @param userId         User id.
     * @param timestampAfter Timestamp after.
     * @param pageSize       Page size.
     * @return {@link TimelinesResponse}.
     */
    private TimelinesResponse retrieveTimelinesByUserId(String userId, Long timestampAfter, Long timestampBefore, int pageSize) {
        TimelineServiceGrpc.TimelineServiceBlockingStub mantellaStub = TimelineServiceGrpc.newBlockingStub(this.mantellaChannel);

        RetrieveMultipleTimelinesRequest.Builder builder = RetrieveMultipleTimelinesRequest.newBuilder()
                .setUserId(userId)
                .setPageRequest(PaginationUtils.buildPageRequest(0, pageSize));
        if (timestampBefore != null) {
            builder.setTimestampBefore(UInt64Value.of(timestampBefore));
        } else {
            builder.setTimestampAfter(timestampAfter == null ? UInt64Value.of(0L) : UInt64Value.of(timestampAfter));
        }

        TimelinesResponse timelinesResponse;
        try {
            timelinesResponse = mantellaStub.retrieveMultipleTimelines(builder.build());
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

    private void buildMediaMessage(NewFeedsRequestV2.Builder builder,
                                   MediaType mediaType,
                                   List<MultiMediaDTO> multiMediaDTOList,
                                   String text,
                                   boolean hasVideoTransCode) {
        switch (mediaType) {
            case MEDIA_PICS:
            case MEDIA_ALBUM:
                builder.setPics(mediaService.picturesMessage(multiMediaDTOList));
                break;
            case MEDIA_HTML:
                builder.setHtml(mediaService.htmlMessage(multiMediaDTOList.get(0)));
                break;
            case MEDIA_VIDEO:
                builder.setVideo(mediaService.videoMessage(multiMediaDTOList.get(0), hasVideoTransCode));
                break;
            case MEDIA_AUDIO:
                builder.setAudio(mediaService.audioMessage(multiMediaDTOList.get(0)));
                break;
        }
        if (!StringUtils.isEmpty(text)) {
            builder.setText(StringValue.of(text));
        }
    }

    private void buildMediaMessage(NewFeedsRequestV2.Builder builder,
                                   MediaType mediaType,
                                   List<MultiMediaDTO> multiMediaDTOList,
                                   String text) {
        buildMediaMessage(builder, mediaType, multiMediaDTOList, text, false);
    }
}
