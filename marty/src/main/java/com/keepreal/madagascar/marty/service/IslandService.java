package com.keepreal.madagascar.marty.service;

import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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

    /**
     * Retrieve device token list by island id.
     *
     * @param islandId      island id.
     * @param pageRequest   {@link PageRequest}.
     * @return  {@link RetrieveDeviceTokensResponse}.
     */
    public RetrieveDeviceTokensResponse getDeviceTokenList(String islandId, PageRequest pageRequest) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.channel);

        RetrieveDeviceTokensRequest request = RetrieveDeviceTokensRequest.newBuilder()
                .setIslandId(islandId)
                .setPageRequest(pageRequest)
                .build();

        RetrieveDeviceTokensResponse response;
        try {
            response = stub.retrieveDeviceTokensById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        return response;
    }
}
