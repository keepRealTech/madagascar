package com.keepreal.madagascar.marty.service;

import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-06-03
 **/

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

        return null;
    }
}
