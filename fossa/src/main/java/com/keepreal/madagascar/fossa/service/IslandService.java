package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.UpdateLastFeedAtRequest;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void callCouaUpdateIslandLastFeedAt(List<String> islandIdList, long timestamp) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);
        UpdateLastFeedAtRequest request = UpdateLastFeedAtRequest.newBuilder()
                .addAllIslandIds(islandIdList)
                .setTimestamps(timestamp)
                .build();

        try {
            stub.updateLastFeedAtById(request);
        } catch (Exception e) {
            log.error("callCouaUpdateIslandLastFeedAt failure! exception: {}", e.getMessage());
        }
    }
}
