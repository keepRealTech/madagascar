package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.CreateDefaultFeedRequest;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Represents the feed service.
 */
@Service
@Slf4j
public class FeedService {

    private final Channel channel;

    /**
     * Constructs the feed service.
     *
     * @param channel GRpc managed channel connection to service Fossa.
     */
    public FeedService(@Qualifier("fossaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Create default feed.
     *
     * @param userId   userId
     * @param hostId   hostId
     * @param islandId islandId
     */
    public void createDefaultFeed(String userId, String hostId, String islandId) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.channel);
        CreateDefaultFeedRequest request = CreateDefaultFeedRequest.newBuilder()
                .setUserId(userId)
                .setHostId(hostId)
                .setIslandId(islandId)
                .build();
        try {
            stub.createDefaultFeed(request);
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }

}
