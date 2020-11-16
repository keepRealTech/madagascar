package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.IslandResponse;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveIslandByIdRequest;
import com.keepreal.madagascar.coua.UpdateLastFeedAtRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the island service.
 */
@Slf4j
@Service
public class IslandService {

    private final Channel channel;

    /**
     * Constructs the island service.
     *
     * @param channel Managed channel for grpc traffic.
     */
    public IslandService(@Qualifier("couaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * update island lastFeedAt by islandIdList
     *
     * @param islandIdList  the islandIdList to be updated.
     * @param timestamp     the timestamp.
     */
    public void callCouaUpdateIslandLastFeedAt(List<String> islandIdList, long timestamp, boolean isWorks) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);
        UpdateLastFeedAtRequest request = UpdateLastFeedAtRequest.newBuilder()
                .addAllIslandIds(islandIdList)
                .setIsWorks(isWorks)
                .setTimestamps(timestamp)
                .build();

        try {
            stub.updateLastFeedAtById(request);
        } catch (Exception e) {
            log.error("callCouaUpdateIslandLastFeedAt failure! exception: {}", e.getMessage());
        }
    }

    public IslandResponse retrieveIslandById(String islandId) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        IslandResponse islandResponse;
        try {
            islandResponse = stub.retrieveIslandById(RetrieveIslandByIdRequest.newBuilder()
                    .setId(islandId)
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(islandResponse)
                || !islandResponse.hasStatus()) {
            log.error(Objects.isNull(islandResponse) ? "Retrieve island returned null." : islandResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        return islandResponse;
    }
}
