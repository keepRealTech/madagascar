package com.keepreal.madagascar.coua.service;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.CreateMembershipSkusRequest;
import com.keepreal.madagascar.vanga.DeleteMembershipSkusByIdRequest;
import com.keepreal.madagascar.vanga.MembershipSkusResponse;
import com.keepreal.madagascar.vanga.SkuServiceGrpc;
import com.keepreal.madagascar.vanga.UpdateMembershipSkusByIdRequest;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
     * Creates membership skus by membership id.
     *
     * @param membershipId   membership id.
     * @param membershipName membership name.
     * @param pricePerMonth  price pre month.
     * @param hostId         host id.
     * @param islandId       island id.
     */
    public void createMembershipSkusByMembershipId(String membershipId,
                                                   String membershipName,
                                                   Integer pricePerMonth,
                                                   String hostId,
                                                   String islandId) {
        SkuServiceGrpc.SkuServiceBlockingStub stub = SkuServiceGrpc.newBlockingStub(this.channel);

        CreateMembershipSkusRequest request = CreateMembershipSkusRequest.newBuilder()
                .setMembershipId(membershipId)
                .setMembershipName(membershipName)
                .setPriceInCentsPerMonth(pricePerMonth)
                .setHostId(hostId)
                .setIslandId(islandId)
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

    /**
     * Updates membership skus by membership id.
     *
     * @param membershipId   Membership id.
     * @param membershipName Membership name.
     * @param pricePerMonth  Price per month.
     * @param active         Active.
     */
    public void updateMembershipSkusByMembershipId(String membershipId, String membershipName, Integer pricePerMonth, Boolean active, Boolean isPermanent) {
        if (StringUtils.isEmpty(membershipName) && Objects.isNull(pricePerMonth) && Objects.isNull(active)) {
            return;
        }

        SkuServiceGrpc.SkuServiceBlockingStub stub = SkuServiceGrpc.newBlockingStub(this.channel);

        UpdateMembershipSkusByIdRequest.Builder requestBuilder = UpdateMembershipSkusByIdRequest.newBuilder()
                .setMembershipId(membershipId);

        if (!StringUtils.isEmpty(membershipName)) {
            requestBuilder.setMembershipName(StringValue.of(membershipName));
        }

        if (!StringUtils.isEmpty(pricePerMonth)) {
            requestBuilder.setPricePerMonth(Int64Value.of(pricePerMonth));
        }

        if (Objects.nonNull(active)) {
            requestBuilder.setActive(BoolValue.of(active));
        }

        if (Objects.nonNull(isPermanent)) {
            requestBuilder.setPermanent(BoolValue.of(isPermanent));
        }

        MembershipSkusResponse response;
        try {
            response = stub.updateMembershipSkusByMembershipId(requestBuilder.build());
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Update skus returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }

    /**
     * Deletes membership skus by membership id.
     *
     * @param membershipId   membership id.
     */
    public void deleteMembershipSkusByMembershipId(String membershipId) {
        SkuServiceGrpc.SkuServiceBlockingStub stub = SkuServiceGrpc.newBlockingStub(this.channel);

        DeleteMembershipSkusByIdRequest request = DeleteMembershipSkusByIdRequest.newBuilder()
                .setMembershipId(membershipId)
                .build();

        CommonStatus response;
        try {
            response = stub.deleteMembershipSkusByMembershipId(request);
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (Objects.isNull(response) || ErrorCode.REQUEST_SUCC_VALUE != response.getRtn()) {
            log.error("Delete membership sku returned null.");
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }

}
