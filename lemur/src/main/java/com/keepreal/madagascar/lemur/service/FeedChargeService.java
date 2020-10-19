package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.Int64Value;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.FeedChargeServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveHasAccessFeedIdRequest;
import com.keepreal.madagascar.vanga.RetrieveHasAccessFeedIdResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the feed charge service.
 */
@Service
@Slf4j
public class FeedChargeService {

    private final Channel channel;

    /**
     * Constructs the feedCharge service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public FeedChargeService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    public List<String> retrieveHasAccessFeedIds(String userId, String islandId, Long timestampBefore, Long timestampAfter) {
        FeedChargeServiceGrpc.FeedChargeServiceBlockingStub stub = FeedChargeServiceGrpc.newBlockingStub(this.channel);

        RetrieveHasAccessFeedIdRequest.Builder builder = RetrieveHasAccessFeedIdRequest.newBuilder()
                .setUserId(userId)
                .setIslandId(islandId);

        if (Objects.nonNull(timestampBefore)) {
            builder.setTimestampBefore(Int64Value.of(timestampBefore));
        } else {
            builder.setTimestampAfter(Int64Value.of(timestampAfter == null ? 0L : timestampAfter));
        }

        RetrieveHasAccessFeedIdResponse response;

        try {
            response = stub.retrieveHasAccessFeedId(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve has access feedIds returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getFeedIdsList();
    }
}
