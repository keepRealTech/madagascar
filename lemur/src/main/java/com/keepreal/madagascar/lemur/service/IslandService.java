package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.IslandAccessType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CheckIslandSubscriptionRequest;
import com.keepreal.madagascar.coua.CheckNameRequest;
import com.keepreal.madagascar.coua.CheckNameResponse;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.coua.CheckNewFeedsRequest;
import com.keepreal.madagascar.coua.CheckNewFeedsResponse;
import com.keepreal.madagascar.coua.DiscoverIslandMessage;
import com.keepreal.madagascar.coua.DiscoverIslandsResponse;
import com.keepreal.madagascar.coua.DismissIntroductionRequest;
import com.keepreal.madagascar.coua.IslandIdentitiesResponse;
import com.keepreal.madagascar.coua.IslandIdentityMessage;
import com.keepreal.madagascar.coua.IslandIdentityServiceGrpc;
import com.keepreal.madagascar.coua.IslandProfileResponse;
import com.keepreal.madagascar.coua.IslandResponse;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.IslandSubscribersResponse;
import com.keepreal.madagascar.coua.IslandSubscriptionStateResponse;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.NewIslandRequest;
import com.keepreal.madagascar.coua.QueryIslandCondition;
import com.keepreal.madagascar.coua.RetrieveDefaultIslandsByUserIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandProfileByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandSubscribersByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslanderPortraitUrlRequest;
import com.keepreal.madagascar.coua.RetrieveIslanderPortraitUrlResponse;
import com.keepreal.madagascar.coua.RetrieveMultipleIslandsRequest;
import com.keepreal.madagascar.coua.RetrieveUserSubscriptionStateRequest;
import com.keepreal.madagascar.coua.RetrieveUserSubscriptionStateResponse;
import com.keepreal.madagascar.coua.SubscribeIslandByIdRequest;
import com.keepreal.madagascar.coua.SubscribeIslandResponse;
import com.keepreal.madagascar.coua.UnsubscribeIslandByIdRequest;
import com.keepreal.madagascar.coua.UpdateIslandByIdRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the island service.
 */
@Service
@Slf4j
public class IslandService {

    private static final int NAME_LENGTH_THRESHOLD = 32;
    private static final int DESCRIPTION_LENGTH_THRESHOLD = 1_000;

    private final Channel channel;

    /**
     * Constructs the island service.
     *
     * @param channel GRpc managed channel connection to service Coua.
     */
    public IslandService(@Qualifier("couaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Checks if a name has been occupied.
     *
     * @param name Name.
     * @return True if occupied.
     */
    public boolean checkName(String name) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        CheckNameRequest request = CheckNameRequest.newBuilder()
                .setName(name)
                .build();

        CheckNameResponse checkNameResponse;
        try {
            checkNameResponse = stub.checkName(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(checkNameResponse)
                || !checkNameResponse.hasStatus()) {
            log.error(Objects.isNull(checkNameResponse) ? "Check name returned null." : checkNameResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != checkNameResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(checkNameResponse.getStatus());
        }

        return checkNameResponse.getIsExisted();
    }

    /**
     * Retrieves island by id.
     *
     * @param id Island id.
     * @return {@link IslandMessage}.
     */
    @Cacheable(value = "IslandMessage", key = "#id", cacheManager = "redisCacheManager")
    public IslandMessage retrieveIslandById(String id) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        RetrieveIslandByIdRequest request = RetrieveIslandByIdRequest.newBuilder()
                .setId(id)
                .build();

        IslandResponse islandResponse;
        try {
            islandResponse = stub.retrieveIslandById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(islandResponse)
                || !islandResponse.hasStatus()) {
            log.error(Objects.isNull(islandResponse) ? "Check island name returned null." : islandResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != islandResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(islandResponse.getStatus());
        }

        return islandResponse.getIsland();
    }

    /**
     * Retrieves island by id.
     *
     * @param id     Island id.
     * @param userId User id.
     * @return {@link IslandProfileResponse}.
     */
    public IslandProfileResponse retrieveIslandProfileById(String id, String userId) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        RetrieveIslandProfileByIdRequest request = RetrieveIslandProfileByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        IslandProfileResponse islandProfileResponse;
        try {
            islandProfileResponse = stub.retrieveIslandProfileById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(islandProfileResponse)
                || !islandProfileResponse.hasStatus()) {
            log.error(Objects.isNull(islandProfileResponse) ? "Check island name returned null." : islandProfileResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != islandProfileResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(islandProfileResponse.getStatus());
        }

        return islandProfileResponse;
    }

    /**
     * Retrieves islands with conditions.
     *
     * @param name         Island name.
     * @param hostId       Island host user id.
     * @param subscriberId Island subscriber id.
     * @param page         Page index.
     * @param pageSize     Page size.
     * @return {@link IslandsResponse}.
     */
    public IslandsResponse retrieveIslands(String name, String hostId, String subscriberId, int page, int pageSize) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        QueryIslandCondition.Builder conditionBuilder = QueryIslandCondition.newBuilder();

        if (Objects.nonNull(name)) {
            conditionBuilder.setName(StringValue.of(name));
        }

        if (!StringUtils.isEmpty(hostId)) {
            conditionBuilder.setHostId(StringValue.of(hostId));
        }

        if (!StringUtils.isEmpty(subscriberId)) {
            conditionBuilder.setSubscribedUserId(StringValue.of(subscriberId));
        }

        RetrieveMultipleIslandsRequest request = RetrieveMultipleIslandsRequest.newBuilder()
                .setCondition(conditionBuilder.build())
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        IslandsResponse islandsResponse;
        try {
            islandsResponse = stub.retrieveIslandsByCondition(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(islandsResponse)
                || !islandsResponse.hasStatus()) {
            log.error(Objects.isNull(islandsResponse) ? "Retrieve islands returned null." : islandsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != islandsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(islandsResponse.getStatus());
        }

        return islandsResponse;
    }

    /**
     * Creates island.
     *
     * @param name             Island name.
     * @param portraitImageUri Portrait image uri.
     * @param secret           Secret.
     * @param identityId       Identity id.
     * @param userId           User id.
     * @param islandAccessType {@link IslandAccessType}.
     * @return {@link IslandMessage}.
     */
    public IslandMessage createIsland(String name,
                                      String portraitImageUri,
                                      String secret,
                                      String identityId,
                                      String userId,
                                      IslandAccessType islandAccessType,
                                      String description,
                                      String customUrl) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        if (Objects.isNull(identityId)) {
            identityId = "";
        }

        if (Objects.isNull(islandAccessType)) {
            islandAccessType = IslandAccessType.ISLAND_ACCESS_PRIVATE;
        }

        NewIslandRequest.Builder requestBuilder = NewIslandRequest.newBuilder()
                .setName(name)
                .setIdentityId(StringValue.of(identityId))
                .setIslandAccessType(islandAccessType)
                .setHostId(userId);

        if (!StringUtils.isEmpty(secret)) {
            requestBuilder.setSecret(StringValue.of(secret));
        }

        if (!StringUtils.isEmpty(portraitImageUri)) {
            requestBuilder.setPortraitImageUri(StringValue.of(portraitImageUri));
        }

        if (!StringUtils.isEmpty(description)) {
            requestBuilder.setDescription(StringValue.of(description));
        }

        if (!StringUtils.isEmpty(customUrl)) {
            requestBuilder.setCustomUrl(StringValue.of(customUrl));
        }

        IslandResponse islandResponse;
        try {
            islandResponse = stub.createIsland(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(islandResponse)
                || !islandResponse.hasStatus()) {
            log.error(Objects.isNull(islandResponse) ? "Create island returned null." : islandResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != islandResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(islandResponse.getStatus());
        }

        return islandResponse.getIsland();
    }

    /**
     * Updates island by id.
     *
     * @param id               Id.
     * @param name             Island name.
     * @param portraitImageUri Portrait image uri.
     * @param secret           Secret.
     * @param description      Description.
     * @param islandAccessType Island access type.
     * @param showIncome whether show income.
     * @param customUrl Island home custom url.
     * @return {@link IslandMessage}.
     */
    public IslandMessage updateIslandById(String id,
                                          String name,
                                          String portraitImageUri,
                                          String secret,
                                          String description,
                                          IslandAccessType islandAccessType,
                                          Boolean showIncome,
                                          String customUrl) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        UpdateIslandByIdRequest.Builder requestBuilder = UpdateIslandByIdRequest.newBuilder()
                .setId(id);

        if (!StringUtils.isEmpty(name)) {
            name = this.checkLength(name, NAME_LENGTH_THRESHOLD);
            requestBuilder.setName(StringValue.of(name));
        }

        if (!StringUtils.isEmpty(portraitImageUri)) {
            requestBuilder.setPortraitImageUri(StringValue.of(portraitImageUri));
        }

        if (Objects.nonNull(islandAccessType)) {
            requestBuilder.setIslandAccessType(islandAccessType);
        }

        if (!StringUtils.isEmpty(secret)) {
            secret = this.checkLength(secret, NAME_LENGTH_THRESHOLD);
            requestBuilder.setSecret(StringValue.of(secret));
        }

        if (!Objects.isNull(description)) {
            description = this.checkLength(description, DESCRIPTION_LENGTH_THRESHOLD);
            requestBuilder.setDescription(StringValue.of(description));
        }

        if (Objects.nonNull(showIncome)) {
            requestBuilder.setShowIncome(BoolValue.of(showIncome));
        }

        if (!StringUtils.isEmpty(customUrl)) {
            requestBuilder.setCustomUrl(StringValue.of(customUrl));
        }

        IslandResponse islandResponse;
        try {
            islandResponse = stub.updateIslandById(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(islandResponse)
                || !islandResponse.hasStatus()) {
            log.error(Objects.isNull(islandResponse) ? "Update island returned null." : islandResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != islandResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(islandResponse.getStatus());
        }

        return islandResponse.getIsland();
    }

    /**
     * Subscribes island by id.
     *
     * @param id     Island id.
     * @param userId User id.
     * @param secret Secret.
     */
    public void subscribeIslandById(String id, String userId, String secret) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        SubscribeIslandByIdRequest.Builder requestBuilder = SubscribeIslandByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId);

        if (Objects.nonNull(secret)) {
            requestBuilder.setSecret(StringValue.of(secret));
        }

        SubscribeIslandResponse subscribeIslandResponse;
        try {
            subscribeIslandResponse = stub.subscribeIslandById(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(subscribeIslandResponse)
                || !subscribeIslandResponse.hasStatus()) {
            log.error(Objects.isNull(subscribeIslandResponse) ? "Subscribe island returned null." : subscribeIslandResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != subscribeIslandResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(subscribeIslandResponse.getStatus());
        }
    }

    /**
     * Unsubscribes island by id.
     *
     * @param id     Island id.
     * @param userId User id.
     */
    public void unsubscribeIslandById(String id, String userId) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        UnsubscribeIslandByIdRequest request = UnsubscribeIslandByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        SubscribeIslandResponse subscribeIslandResponse;
        try {
            subscribeIslandResponse = stub.unsubscribeIslandById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(subscribeIslandResponse)
                || !subscribeIslandResponse.hasStatus()) {
            log.error(Objects.isNull(subscribeIslandResponse) ? "Unsubscribe island returned null." : subscribeIslandResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != subscribeIslandResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(subscribeIslandResponse.getStatus());
        }
    }

    /**
     * Retrieves the subscribers of an island.
     *
     * @param id       Island id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link IslandSubscribersResponse}.
     */
    public IslandSubscribersResponse retrieveSubscriberByIslandId(String id, int page, int pageSize) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        RetrieveIslandSubscribersByIdRequest request = RetrieveIslandSubscribersByIdRequest.newBuilder()
                .setId(id)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        IslandSubscribersResponse islandSubscribersResponse;
        try {
            islandSubscribersResponse = stub.retrieveIslandSubscribersById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(islandSubscribersResponse)
                || !islandSubscribersResponse.hasStatus()) {
            log.error(Objects.isNull(islandSubscribersResponse) ? "Retrieve island subscribers returned null." : islandSubscribersResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != islandSubscribersResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(islandSubscribersResponse.getStatus());
        }

        return islandSubscribersResponse;
    }

    /**
     * Checks if has new feeds after the given timestamp.
     *
     * @param islandIds  Island ids.
     * @param timestamps Timestamps in milli-seconds.
     * @return List of {@link CheckNewFeedsMessage}.
     */
    public List<CheckNewFeedsMessage> checkNewFeeds(List<String> islandIds, List<Long> timestamps) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        CheckNewFeedsRequest request = CheckNewFeedsRequest.newBuilder()
                .addAllIslandIds(islandIds)
                .addAllTimestamps(timestamps)
                .build();

        CheckNewFeedsResponse checkNewFeedsResponse;
        try {
            checkNewFeedsResponse = stub.checkNewFeeds(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
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

    /**
     * Retrieves the default islands for posting new feeds.
     *
     * @param userId   User id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link IslandsResponse}.
     */
    public IslandsResponse retrieveDefaultIslands(String userId, String islandId, int page, int pageSize) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        RetrieveDefaultIslandsByUserIdRequest.Builder builder = RetrieveDefaultIslandsByUserIdRequest.newBuilder();
        builder.setUserId(userId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize));
        if (!StringUtils.isEmpty(islandId)) {
            builder.setIslandId(StringValue.of(islandId));
        }

        IslandsResponse islandsResponse;
        try {
            islandsResponse = stub.retrieveDefaultIslandsByUserId(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(islandsResponse)
                || !islandsResponse.hasStatus()) {
            log.error(Objects.isNull(islandsResponse) ? "Retrieve default islands null." : islandsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != islandsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(islandsResponse.getStatus());
        }

        return islandsResponse;
    }

    /**
     * Retrieves the island subscribe state by user id.
     *
     * @param userId       User id.
     * @param islandIdList Island ids.
     * @return True if subscribed.
     */
    public Map<String, Boolean> retrieveIslandSubscribeStateByUserId(String userId, Collection<String> islandIdList) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        RetrieveUserSubscriptionStateRequest request = RetrieveUserSubscriptionStateRequest.newBuilder()
                .setUserId(userId)
                .addAllIslandIds(islandIdList)
                .build();

        RetrieveUserSubscriptionStateResponse response;
        try {
            response = stub.retrieveUserSubscriptionState(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve island subscribe state returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getStateMapMap();
    }

    /**
     * Retrieves all active island identities.
     *
     * @return {@link IslandIdentityMessage}.
     */
    public List<IslandIdentityMessage> retrieveActiveIslandIdentities() {
        IslandIdentityServiceGrpc.IslandIdentityServiceBlockingStub stub = IslandIdentityServiceGrpc.newBlockingStub(this.channel);

        IslandIdentitiesResponse response;
        try {
            response = stub.retrieveActiveIslandIdentities(Empty.getDefaultInstance());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve island subscribe state returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getIslandIdentitiesList();
    }

    /**
     * Dismisses the island host introduction once and for all.
     *
     * @param islandId     Island id.
     * @param userId       User id.
     * @param isIslandHost Whether it is the host.
     */
    public void dismissIslandIntroduction(String islandId, String userId, Boolean isIslandHost) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        DismissIntroductionRequest request = DismissIntroductionRequest.newBuilder()
                .setUserId(userId)
                .setIslandId(islandId)
                .setIsIslandHost(isIslandHost)
                .build();

        CommonStatus response;
        try {
            response = stub.dismissIntroduction(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)) {
            log.error("Dismiss island introduction returned null.");
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getRtn()) {
            throw new KeepRealBusinessException(response);
        }
    }

    /**
     * Checks if the user id is a subscriber of island.
     *
     * @param islandId Island id.
     * @param userId   User id.
     * @return True if an islander or host.
     */
    public boolean checkIslandSubscription(String islandId, String userId) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        CheckIslandSubscriptionRequest request = CheckIslandSubscriptionRequest.newBuilder()
                .setUserId(userId)
                .setIslandId(islandId)
                .build();

        IslandSubscriptionStateResponse response;
        try {
            response = stub.checkIslandSubscription(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve island subscribe state returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getHasSubscribed();
    }

    /**
     * Retrieves the islander portraits.
     *
     * @param islandId Island id.
     * @return Portrait uris.
     */
    public List<String> retrieveIslanderPortraitUrlByIslandId(String islandId) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        RetrieveIslanderPortraitUrlRequest request = RetrieveIslanderPortraitUrlRequest.newBuilder()
                .setIslandId(islandId)
                .build();

        RetrieveIslanderPortraitUrlResponse response;
        try {
            response = stub.retrieveIslanderPortraitUrlByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve islander portraits returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getPortraitUrlList();
    }

    /**
     * Retrieves islands discovery.
     *
     * @return {@link DiscoverIslandMessage}.
     */
    public List<DiscoverIslandMessage> retrieveIslandsInDiscovery() {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        DiscoverIslandsResponse response;
        try {
            response = stub.discoverIslands(Empty.getDefaultInstance());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve islands discovery returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getDicoverIslandsList();
    }

    /**
     * 根据hostId 获取创建的岛
     *
     * @param hostId    host id
     * @return          {@link List<IslandMessage>}
     */
    public List<IslandMessage> retrieveIslandsByHostId(String hostId) {
        return this.retrieveIslands(null, hostId, null, 0, 10).getIslandsList();
    }

    /**
     * Checks the string length.
     *
     * @param str       String.
     * @param threshold Max length.
     * @return Trimmed string.
     */
    private String checkLength(String str, int threshold) {
        String trimmed = str.trim();
        if (trimmed.length() > threshold)
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        return trimmed;
    }
}
