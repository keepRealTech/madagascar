package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CheckNameRequest;
import com.keepreal.madagascar.coua.CheckNameResponse;
import com.keepreal.madagascar.coua.IslandProfileResponse;
import com.keepreal.madagascar.coua.IslandResponse;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.IslandSubscribersResponse;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.NewIslandRequest;
import com.keepreal.madagascar.coua.QueryIslandCondition;
import com.keepreal.madagascar.coua.RetrieveIslandByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandProfileByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandSubscribersByIdRequest;
import com.keepreal.madagascar.coua.RetrieveMultipleIslandsRequest;
import com.keepreal.madagascar.coua.SubscribeIslandByIdRequest;
import com.keepreal.madagascar.coua.SubscribeIslandResponse;
import com.keepreal.madagascar.coua.UnsubscribeIslandByIdRequest;
import com.keepreal.madagascar.coua.UpdateIslandByIdRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Represents the island service.
 */
@Service
@Slf4j
public class IslandService {

    private final ManagedChannel managedChannel;

    /**
     * Constructs the island service.
     *
     * @param managedChannel GRpc managed channel connection to service Coua.
     */
    public IslandService(@Qualifier("couaChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * Checks if a name has been occupied.
     *
     * @param name Name.
     * @return True if occupied.
     */
    public boolean checkName(String name) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);

        CheckNameRequest request = CheckNameRequest.newBuilder()
                .setName(name)
                .build();

        CheckNameResponse checkNameResponse;
        try {
            checkNameResponse = stub.checkName(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
    public IslandMessage retrieveIslandById(String id) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);

        RetrieveIslandByIdRequest request = RetrieveIslandByIdRequest.newBuilder()
                .setId(id)
                .build();

        IslandResponse islandResponse;
        try {
            islandResponse = stub.retrieveIslandById(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);

        RetrieveIslandProfileByIdRequest request = RetrieveIslandProfileByIdRequest.newBuilder()
                .setId(id)
                .setUserId(StringValue.of(userId))
                .build();

        IslandProfileResponse islandProfileResponse;
        try {
            islandProfileResponse = stub.retrieveIslandProfileById(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);

        QueryIslandCondition.Builder conditionBuilder = QueryIslandCondition.newBuilder();

        if (!StringUtils.isEmpty(name)) {
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
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
     * @return {@link IslandMessage}.
     */
    public IslandMessage createIsland(String name, String portraitImageUri, String secret) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);

        NewIslandRequest request = NewIslandRequest.newBuilder()
                .setName(name)
                .setPortraitImageUri(StringValue.of(portraitImageUri))
                .setSecret(StringValue.of(secret))
                .build();

        IslandResponse islandResponse;
        try {
            islandResponse = stub.createIsland(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
     * @return {@link IslandMessage}.
     */
    public IslandMessage updateIslandById(String id, String name, String portraitImageUri, String secret, String description) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);

        UpdateIslandByIdRequest.Builder requestBuilder = UpdateIslandByIdRequest.newBuilder()
                .setId(id);

        if (!StringUtils.isEmpty(name)) {
            requestBuilder.setName(StringValue.of(name));
        }

        if (!StringUtils.isEmpty(portraitImageUri)) {
            requestBuilder.setPortraitImageUri(StringValue.of(portraitImageUri));
        }

        if (!StringUtils.isEmpty(secret)) {
            requestBuilder.setSecret(StringValue.of(secret));
        }

        if (!StringUtils.isEmpty(description)) {
            requestBuilder.setDescription(StringValue.of(description));
        }

        IslandResponse islandResponse;
        try {
            islandResponse = stub.updateIslandById(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);

        SubscribeIslandByIdRequest request = SubscribeIslandByIdRequest.newBuilder()
                .setId(id)
                .setSecret(secret)
                .setUserId(userId)
                .build();

        SubscribeIslandResponse subscribeIslandResponse;
        try {
            subscribeIslandResponse = stub.subscribeIslandById(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);

        UnsubscribeIslandByIdRequest request = UnsubscribeIslandByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        SubscribeIslandResponse subscribeIslandResponse;
        try {
            subscribeIslandResponse = stub.unsubscribeIslandById(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);

        RetrieveIslandSubscribersByIdRequest request = RetrieveIslandSubscribersByIdRequest.newBuilder()
                .setId(id)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        IslandSubscribersResponse islandSubscribersResponse;
        try {
            islandSubscribersResponse = stub.retrieveIslandSubscribersById(request);
        } catch (StatusRuntimeException exception) {
            throw new throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage()););
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

}
