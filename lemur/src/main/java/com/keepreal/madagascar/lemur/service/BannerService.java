package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.enums.BannerType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.hoopoe.BannerMessage;
import com.keepreal.madagascar.hoopoe.BannerResponse;
import com.keepreal.madagascar.hoopoe.BannerServiceGrpc;
import com.keepreal.madagascar.hoopoe.RetrieveBannerRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the banner service.
 */
@Service
@Slf4j
public class BannerService {
    private final Channel channel;

    /**
     * Constructs the banner service
     *
     * @param channel {@link Channel}
     */
    public BannerService(@Qualifier("hoopoeChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * 根据类型获取banner
     *
     * @param userId user id
     * @param bannerType {@link BannerType}
     * @return {@link BannerMessage}
     */
    public List<BannerMessage> retrieveBanners(String userId, BannerType bannerType) {
        BannerServiceGrpc.BannerServiceBlockingStub stub = BannerServiceGrpc.newBlockingStub(this.channel);

        RetrieveBannerRequest request = RetrieveBannerRequest.newBuilder().setUserId(userId).setBannerType(bannerType.getValue()).build();

        BannerResponse response;

        try {
            response = stub.retrieveBanner(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve banner by type returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getBannersList();
    }

}
