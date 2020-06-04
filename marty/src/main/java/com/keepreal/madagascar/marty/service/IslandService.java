package com.keepreal.madagascar.marty.service;

import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Represents the island service.
 */
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

    public List<String> getDeviceTokenList(String islandId) {
        // TODO get android & ios device token
        return null;
    }
}
