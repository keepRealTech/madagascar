package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.hoopoe.ActiveBannerMessage;
import com.keepreal.madagascar.hoopoe.ActivityServiceGrpc;
import com.keepreal.madagascar.hoopoe.RetrieveActiveBannerRequest;
import com.keepreal.madagascar.hoopoe.RetrieveActiveBannerResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the activity service.
 */
@Service
@Slf4j
public class ActivityService {

    private final Channel channel;

    /**
     * Constructs the activity service.
     *
     * @param channel GRpc managed channel connection to service Hoopoe.
     */
    public ActivityService(@Qualifier("hoopoeChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * 获取活动banner
     *
     * @return  {@link ActiveBannerMessage}
     */
    public List<ActiveBannerMessage> retrieveActiveBanner(Boolean isIslandHost) {
        ActivityServiceGrpc.ActivityServiceBlockingStub stub = ActivityServiceGrpc.newBlockingStub(this.channel);
        RetrieveActiveBannerRequest request = RetrieveActiveBannerRequest.newBuilder()
                .setIsIslandHost(isIslandHost)
                .build();
        RetrieveActiveBannerResponse response;

        try {
            response = stub.retrieveActiveBanner(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve activity banner returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getActiveBannersList();
    }

}