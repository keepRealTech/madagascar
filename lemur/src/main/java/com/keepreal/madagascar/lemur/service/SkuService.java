package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.RetrieveSponsorRequest;
import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.MembershipSkusResponse;
import com.keepreal.madagascar.vanga.RetrieveMembershipSkusByMembershipIdRequest;
import com.keepreal.madagascar.vanga.RetrieveShellSkusRequest;
import com.keepreal.madagascar.vanga.RetrieveSponsorSkusRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportSkusRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportSkusResponse;
import com.keepreal.madagascar.vanga.ShellSkuMessage;
import com.keepreal.madagascar.vanga.ShellSkusResponse;
import com.keepreal.madagascar.vanga.SkuServiceGrpc;
import com.keepreal.madagascar.vanga.SponsorSkuMessage;
import com.keepreal.madagascar.vanga.SponsorSkusResponse;
import com.keepreal.madagascar.vanga.SupportSkuMessage;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import swagger.model.SupportSkusResponse;

import java.util.List;
import java.util.Objects;

/**
 * Represents the shell sku service.
 */
@Service
@Slf4j
public class SkuService {

    private final Channel channel;

    /**
     * Constructs the shell sku service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public SkuService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Retrieves the active shell skus.
     *
     * @param isWechatPay Whether is wechat pay.
     * @return {@link ShellSkusResponse}.
     */
    @Cacheable(value = "Shellsku", key = "#isWechatPay", cacheManager = "ehcacheCacheManager")
    public List<ShellSkuMessage> retrieveShellSkus(Boolean isWechatPay) {
        SkuServiceGrpc.SkuServiceBlockingStub stub = SkuServiceGrpc.newBlockingStub(this.channel);

        RetrieveShellSkusRequest request = RetrieveShellSkusRequest.newBuilder()
                .setIsWechatPay(isWechatPay)
                .build();

        ShellSkusResponse shellSkusResponse;
        try {
            shellSkusResponse = stub.retrieveActiveShellSkus(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(shellSkusResponse)
                || !shellSkusResponse.hasStatus()) {
            log.error(Objects.isNull(shellSkusResponse) ? "Retrieve shell skus returned null." : shellSkusResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != shellSkusResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(shellSkusResponse.getStatus());
        }

        return shellSkusResponse.getShellSkusList();
    }

    /**
     * Retrieves the active membership skus for given membership id.
     *
     * @param membershipId Membership id.
     * @return {@link MembershipSkuMessage}.
     */
    public List<MembershipSkuMessage> retrieveMembershipSkusByMembershipIds(String membershipId) {
        SkuServiceGrpc.SkuServiceBlockingStub stub = SkuServiceGrpc.newBlockingStub(this.channel);

        RetrieveMembershipSkusByMembershipIdRequest request = RetrieveMembershipSkusByMembershipIdRequest.newBuilder()
                .setMembershipId(membershipId)
                .build();

        MembershipSkusResponse membershipSkusResponse;
        try {
            membershipSkusResponse = stub.retrieveActiveMembershipSkusByMembershipId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(membershipSkusResponse)
                || !membershipSkusResponse.hasStatus()) {
            log.error(Objects.isNull(membershipSkusResponse) ? "Retrieve membership skus returned null." : membershipSkusResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != membershipSkusResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(membershipSkusResponse.getStatus());
        }

        return membershipSkusResponse.getMembershipSkusList();
    }

    public List<SupportSkuMessage> retrieveSupportSkus() {
        SkuServiceGrpc.SkuServiceBlockingStub stub = SkuServiceGrpc.newBlockingStub(this.channel);

        RetrieveSupportSkusResponse response;

        try {
            response = stub.retrieveActiveSupportSkus(RetrieveSupportSkusRequest.newBuilder().build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve support skus returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getSupportSkusList();
    }

    /**
     * 获取支持一下sku信息
     *
     * @return {@link SponsorSkusResponse}
     */
    public SponsorSkusResponse retrieveSponsorSkus(String islandId) {
        SkuServiceGrpc.SkuServiceBlockingStub stub = SkuServiceGrpc.newBlockingStub(this.channel);

        SponsorSkusResponse response;

        try {
            response = stub.retrieveSponsorSkusByIslandId(RetrieveSponsorSkusRequest.newBuilder().setIslandId(islandId).build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve sponsor skus returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

}
