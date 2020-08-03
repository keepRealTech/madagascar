package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.FeedRepostMessage;
import com.keepreal.madagascar.fossa.FeedRepostResponse;
import com.keepreal.madagascar.fossa.FeedRepostsResponse;
import com.keepreal.madagascar.fossa.GenerateRepostCodeRequest;
import com.keepreal.madagascar.fossa.GenerateRepostCodeResponse;
import com.keepreal.madagascar.fossa.IslandRepostMessage;
import com.keepreal.madagascar.fossa.IslandRepostResponse;
import com.keepreal.madagascar.fossa.IslandRepostsResponse;
import com.keepreal.madagascar.fossa.NewFeedRepostRequest;
import com.keepreal.madagascar.fossa.NewIslandRepostRequest;
import com.keepreal.madagascar.fossa.RepostServiceGrpc;
import com.keepreal.madagascar.fossa.ResolveRepostCodeRequest;
import com.keepreal.madagascar.fossa.ResolveRepostCodeResponse;
import com.keepreal.madagascar.fossa.RetrieveFeedRepostsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RetrieveIslandRepostsByIslandIdRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import swagger.model.DeviceType;

import java.util.Objects;

/**
 * Represents the repost service.
 */
@Service
@Slf4j
public class RepostService {

    private final Channel channel;

    /**
     * Constructs the repost service.
     *
     * @param channel GRpc managed channel connection to service fossa.
     */
    public RepostService(@Qualifier("fossaChannel") Channel channel) {
        this.channel = channel;
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
        RepostServiceGrpc.RepostServiceBlockingStub stub = RepostServiceGrpc.newBlockingStub(this.channel);

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
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
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
        RepostServiceGrpc.RepostServiceBlockingStub stub = RepostServiceGrpc.newBlockingStub(this.channel);

        RetrieveIslandRepostsByIslandIdRequest request = RetrieveIslandRepostsByIslandIdRequest.newBuilder()
                .setIslandId(islandId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        IslandRepostsResponse repostsResponse;
        try {
            repostsResponse = stub.retrieveIslandRepostsByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
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

    /**
     * Creates a new feed repost.
     *
     * @param feedId    Feed id.
     * @param userId    User id.
     * @param content   Content.
     * @param succeeded Succeeded or not.
     * @return {@link FeedRepostMessage}.
     */
    public FeedRepostMessage createRepostFeedById(String feedId, String userId, String content, boolean succeeded) {
        RepostServiceGrpc.RepostServiceBlockingStub stub = RepostServiceGrpc.newBlockingStub(this.channel);

        NewFeedRepostRequest request = NewFeedRepostRequest.newBuilder()
                .setFeedId(feedId)
                .setContent(content)
                .setUserId(userId)
                .setIsSuccessful(succeeded)
                .build();

        FeedRepostResponse repostResponse;
        try {
            repostResponse = stub.createFeedRepost(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(repostResponse)
                || !repostResponse.hasStatus()) {
            log.error(Objects.isNull(repostResponse) ? "Create feed repost returned null." : repostResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != repostResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(repostResponse.getStatus());
        }

        return repostResponse.getFeedRepost();
    }

    /**
     * Retrieves feed reposts.
     *
     * @param feedId   Feed id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link FeedRepostsResponse}.
     */
    public FeedRepostsResponse retrieveRepostFeedById(String feedId, int page, int pageSize) {
        RepostServiceGrpc.RepostServiceBlockingStub stub = RepostServiceGrpc.newBlockingStub(this.channel);

        RetrieveFeedRepostsByFeedIdRequest request = RetrieveFeedRepostsByFeedIdRequest.newBuilder()
                .setFeedId(feedId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        FeedRepostsResponse repostsResponse;
        try {
            repostsResponse = stub.retrieveFeedRepostsByFeedId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(repostsResponse)
                || !repostsResponse.hasStatus()) {
            log.error(Objects.isNull(repostsResponse) ? "Retrieve feed reposts returned null." : repostsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != repostsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(repostsResponse.getStatus());
        }

        return repostsResponse;
    }

    public String generateRepostCode(String islandId, String userId) {
        RepostServiceGrpc.RepostServiceBlockingStub stub = RepostServiceGrpc.newBlockingStub(this.channel);

        GenerateRepostCodeRequest request = GenerateRepostCodeRequest.newBuilder()
                .setUserId(userId)
                .setIslandId(islandId)
                .build();

        GenerateRepostCodeResponse response;
        try {
            response = stub.generateRepostCodeByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "generate repost code returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getCode();

    }

    public ResolveRepostCodeResponse resolveRepostCode(String code, DeviceType deviceType) {
        RepostServiceGrpc.RepostServiceBlockingStub stub = RepostServiceGrpc.newBlockingStub(this.channel);

        ResolveRepostCodeRequest request = ResolveRepostCodeRequest.newBuilder()
                .setCode(code)
                .setDeviceType(deviceType.equals(DeviceType.ANDROID) ?
                        com.keepreal.madagascar.common.DeviceType.ANDROID :
                        com.keepreal.madagascar.common.DeviceType.IOS)
                .build();

        ResolveRepostCodeResponse response;
        try {
            response = stub.resolveRepostCode(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "resolve repost code returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

}
