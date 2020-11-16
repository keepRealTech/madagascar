package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.Int64Value;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.AddFeedToCollectionRequest;
import com.keepreal.madagascar.fossa.CollectedFeedsResponse;
import com.keepreal.madagascar.fossa.FeedCollectionServiceGrpc;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.fossa.RemoveFeedToCollectionRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedsByUserIdRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class FeedCollectionService {

    private final Channel fossaChannel;

    public FeedCollectionService(@Qualifier("fossaChannel") Channel fossaChannel) {
        this.fossaChannel = fossaChannel;
    }

    public void addFeedToCollection(String userId, String feedId) {
        FeedCollectionServiceGrpc.FeedCollectionServiceBlockingStub stub = FeedCollectionServiceGrpc.newBlockingStub(this.fossaChannel);

        AddFeedToCollectionRequest request = AddFeedToCollectionRequest.newBuilder()
                .setUserId(userId)
                .setFeedId(feedId)
                .build();

        CommonStatus response;
        try {
            response = stub.addFeedToCollection(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getRtn()) {
            throw new KeepRealBusinessException(response);
        }
    }

    public void removeFeedToCollection(String userId, String feedId) {
        FeedCollectionServiceGrpc.FeedCollectionServiceBlockingStub stub = FeedCollectionServiceGrpc.newBlockingStub(this.fossaChannel);

        RemoveFeedToCollectionRequest request = RemoveFeedToCollectionRequest.newBuilder()
                .setUserId(userId)
                .setFeedId(feedId)
                .build();

        CommonStatus response;
        try {
            response = stub.removeFeedToCollection(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getRtn()) {
            throw new KeepRealBusinessException(response);
        }
    }

    public CollectedFeedsResponse retrieveFeedsByUserId(String userId,
                                               int pageSize,
                                               Long minTimestamp,
                                               Long maxTimestamp) {
        FeedCollectionServiceGrpc.FeedCollectionServiceBlockingStub stub = FeedCollectionServiceGrpc.newBlockingStub(this.fossaChannel);

        RetrieveFeedsByUserIdRequest.Builder builder = RetrieveFeedsByUserIdRequest.newBuilder()
                .setUserId(userId)
                .setPageSize(pageSize);

        if (Objects.nonNull(minTimestamp)) {
            builder.setTimestampAfter(Int64Value.of(minTimestamp));
        }

        if (Objects.nonNull(maxTimestamp)) {
            builder.setTimestampBefore(Int64Value.of(maxTimestamp));
        }

        CollectedFeedsResponse response;

        try {
            response = stub.retrieveFeedsByUserId(builder.build());
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
}
