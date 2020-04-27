package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.IslandRepostMessage;
import com.keepreal.madagascar.coua.IslandRepostResponse;
import com.keepreal.madagascar.coua.IslandRepostsResponse;
import com.keepreal.madagascar.coua.NewIslandRepostRequest;
import com.keepreal.madagascar.coua.RepostServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveIslandRepostsByIslandIdRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the repost service.
 */
@Service
@Slf4j
public class RepostService {

    private final ManagedChannel managedChannel;

    /**
     * Constructs the repost service.
     *
     * @param managedChannel GRpc managed channel connection to service Coua.
     */
    public RepostService(@Qualifier("couaChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * Creates a new island repost.
     *
     * @param islandId  Island id.
     * @param userId    User id.
     * @param content   Content.
     * @param succeeded Succeeded or not.
     * @return {@link IslandRepostMessage}.
     */
    public IslandRepostMessage createRepostIslandById(String islandId, String userId, String content, boolean succeeded) {
        RepostServiceGrpc.RepostServiceBlockingStub stub = RepostServiceGrpc.newBlockingStub(this.managedChannel);

        NewIslandRepostRequest request = NewIslandRepostRequest.newBuilder()
                .setIslandId(islandId)
                .setContent(content)
                .setUserId(userId)
                .setIsSuccessful(succeeded)
                .build();

        IslandRepostResponse repostResponse;
        try {
            repostResponse = stub.createIslandRepost(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(exception);
        }

        if (Objects.isNull(repostResponse)
                || !repostResponse.hasStatus()) {
            log.error(Objects.isNull(repostResponse) ? "Create island repost returned null." : repostResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != repostResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(repostResponse.getStatus());
        }

        return repostResponse.getIslandRepost();
    }

    /**
     * Retrieves island reposts.
     *
     * @param islandId Island id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link IslandRepostsResponse}.
     */
    public IslandRepostsResponse retrieveRepostIslandById(String islandId, int page, int pageSize) {
        RepostServiceGrpc.RepostServiceBlockingStub stub = RepostServiceGrpc.newBlockingStub(this.managedChannel);

        RetrieveIslandRepostsByIslandIdRequest request = RetrieveIslandRepostsByIslandIdRequest.newBuilder()
                .setIslandId(islandId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        IslandRepostsResponse repostsResponse;
        try {
            repostsResponse = stub.retrieveIslandRepostsByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(exception);
        }

        if (Objects.isNull(repostsResponse)
                || !repostsResponse.hasStatus()) {
            log.error(Objects.isNull(repostsResponse) ? "Retrieve island reposts returned null." : repostsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != repostsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(repostsResponse.getStatus());
        }

        return repostsResponse;
    }

}
