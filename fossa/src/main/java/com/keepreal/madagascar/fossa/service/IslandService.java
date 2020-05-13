package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.UpdateLastFeedAtRequest;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Represents the user service.
 */
@Slf4j
@Service
public class IslandService {

    private final ManagedChannel managedChannel;

    /**
     * Constructs the user service.
     *
     * @param managedChannel Managed channel for grpc traffic.
     */
    public IslandService(@Qualifier("couaChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * update island lastFeedAt by islandIdList
     *
     * @param islandIdList  the islandIdList to be updated
     */
    public void callCouaUpdateIslandLastFeedAt(List<String> islandIdList) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);
        UpdateLastFeedAtRequest request = UpdateLastFeedAtRequest.newBuilder()
                .addAllIslandIds(islandIdList)
                .setTimestamps(System.currentTimeMillis())
                .build();

        try {
            stub.updateLastFeedAtById(request);
        } catch (Exception e) {
            log.error("callCouaUpdateIslandLastFeedAt failure! exception: {}", e.getMessage());
        }
    }
}
