package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.CreateMembershipSkusRequest;
import com.keepreal.madagascar.vanga.MembershipSkusResponse;
import com.keepreal.madagascar.vanga.SkuServiceGrpc;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the sku service.
 */
@Service
@Slf4j
public class SkuService {

    private final Channel channel;

    /**
     * Constructs the sku service.
     *
     * @param channel GRpc managed channel connection to service Sku.
     */
    public SkuService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Create membership skus by membership id.
     *
     * @param membershipId  membership id.
     * @param pricePreMonth price pre month.
     */
    public void createMembershipSkusByMembershipId(String membershipId, Integer pricePreMonth) {
        SkuServiceGrpc.SkuServiceBlockingStub stub = SkuServiceGrpc.newBlockingStub(this.channel);

        CreateMembershipSkusRequest request = CreateMembershipSkusRequest.newBuilder()
                .setMembershipId(membershipId)
                .setPriceInCentsPerMonth(pricePreMonth)
                .build();

        MembershipSkusResponse response;
        try {
            response = stub.createMembershipSkusByMembershipId(request);
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Create skus returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }
}
