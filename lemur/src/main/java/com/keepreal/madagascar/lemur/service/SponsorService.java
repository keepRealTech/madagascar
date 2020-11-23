package com.keepreal.madagascar.lemur.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt64Value;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.RetrieveSingleSponsorGiftRequest;
import com.keepreal.madagascar.coua.RetrieveSingleSponsorGiftResponse;
import com.keepreal.madagascar.coua.RetrieveSponsorByHostIdRequest;
import com.keepreal.madagascar.coua.RetrieveSponsorGiftsRequest;
import com.keepreal.madagascar.coua.RetrieveSponsorGiftsResponse;
import com.keepreal.madagascar.coua.RetrieveSponsorRequest;
import com.keepreal.madagascar.coua.RetrieveSponsorResponse;
import com.keepreal.madagascar.coua.SponsorGiftMessage;
import com.keepreal.madagascar.coua.SponsorMessage;
import com.keepreal.madagascar.coua.SponsorServiceGrpc;
import com.keepreal.madagascar.coua.UpdateSponsorByIslandIdRequest;
import com.keepreal.madagascar.vanga.RetrieveSponsorHistoryRequest;
import com.keepreal.madagascar.vanga.RetrieveSponsorHistoryResponse;
import com.keepreal.madagascar.vanga.SponsorHistoryServiceGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Represents the sponsor service.
 */
@Service
@Slf4j
public class SponsorService {
    private final Channel couaChannel;
    private final Channel vangaChannel;

    /**
     * Constructs the sponsor service.
     *
     * @param couaChannel GRpc managed channel connection to service Coua.
     * @param vangaChannel GRpc managed channel connection to service Vanga.
     */
    public SponsorService(@Qualifier("couaChannel") Channel couaChannel,
                          @Qualifier("vangaChannel") Channel vangaChannel) {
        this.couaChannel = couaChannel;
        this.vangaChannel = vangaChannel;
    }

    /**
     * 根据岛id 获取支持一下信息
     *
     * @param islandId 岛id
     * @return {@link SponsorMessage}
     */
    public SponsorMessage retrieveSponsorByIslandId(String islandId) {
        SponsorServiceGrpc.SponsorServiceBlockingStub stub = SponsorServiceGrpc.newBlockingStub(this.couaChannel);

        RetrieveSponsorRequest request = RetrieveSponsorRequest.newBuilder()
                .setIslandId(islandId)
                .build();

        RetrieveSponsorResponse retrieveSponsorResponse;
        try {
            retrieveSponsorResponse = stub.retrieveSponsor(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(retrieveSponsorResponse)
                || !retrieveSponsorResponse.hasStatus()) {
            log.error(Objects.isNull(retrieveSponsorResponse) ? "Retrieve sponsor returned null." : retrieveSponsorResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != retrieveSponsorResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(retrieveSponsorResponse.getStatus());
        }

        return retrieveSponsorResponse.getSponsorMessage();
    }

    public SponsorMessage retrieveSponsorByHostId(String hostId) {
        SponsorServiceGrpc.SponsorServiceBlockingStub stub = SponsorServiceGrpc.newBlockingStub(this.couaChannel);

        RetrieveSponsorByHostIdRequest request = RetrieveSponsorByHostIdRequest.newBuilder()
                .setHostId(hostId)
                .build();

        RetrieveSponsorResponse retrieveSponsorResponse;
        try {
            retrieveSponsorResponse = stub.retrieveSponsorByHostId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(retrieveSponsorResponse)
                || !retrieveSponsorResponse.hasStatus()) {
            log.error(Objects.isNull(retrieveSponsorResponse) ? "Retrieve sponsor by hostId returned null." : retrieveSponsorResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != retrieveSponsorResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(retrieveSponsorResponse.getStatus());
        }

        return retrieveSponsorResponse.getSponsorMessage();
    }

    /**
     * 获取礼物表情列表
     *
     * @param onlyDefault 是否只获取预设礼物表情
     * @return {@link SponsorMessage}
     */
    public List<SponsorGiftMessage> retrieveSponsorGiftsByCondition(boolean onlyDefault) {
        SponsorServiceGrpc.SponsorServiceBlockingStub stub = SponsorServiceGrpc.newBlockingStub(this.couaChannel);

        RetrieveSponsorGiftsRequest request = RetrieveSponsorGiftsRequest.newBuilder()
                .setOnlyDefault(onlyDefault)
                .build();

        RetrieveSponsorGiftsResponse response;
        try {
            response = stub.retrieveSponsorGifts(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve sponsor gifts returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getSponsorGiftsList();
    }

    /**
     * 根据id获取表情
     *
     * @param giftId gift id
     * @return {@link SponsorGiftMessage}
     */
    public SponsorGiftMessage retrieveSponsorGiftByGiftId(String giftId) {
        SponsorServiceGrpc.SponsorServiceBlockingStub stub = SponsorServiceGrpc.newBlockingStub(this.couaChannel);

        RetrieveSingleSponsorGiftRequest request = RetrieveSingleSponsorGiftRequest.newBuilder()
                .setGiftId(giftId)
                .build();

        RetrieveSingleSponsorGiftResponse response;
        try {
            response = stub.retrieveSingleSponsorGift(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve single sponsor gift returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getSponsorGift();
    }

    /**
     * 更新支持一下信息
     *
     * @param islandId 岛id
     * @param description 支持一下描述
     * @param giftId 礼物id
     * @param pricePerUnit 礼物单价
     * @return {@link SponsorGiftMessage}
     */
    public SponsorMessage updateSponsorGiftByIslandId(String islandId,
                                                      String description,
                                                      String giftId,
                                                      Long pricePerUnit) {

        SponsorServiceGrpc.SponsorServiceBlockingStub stub = SponsorServiceGrpc.newBlockingStub(this.couaChannel);

        UpdateSponsorByIslandIdRequest.Builder builder = UpdateSponsorByIslandIdRequest.newBuilder()
                .setIslandId(islandId);

        if (!StringUtils.isEmpty(description)) {
            builder.setDescription(StringValue.of(description));
        }

        if (!StringUtils.isEmpty(giftId)) {
            try {
                Integer.valueOf(giftId);
            } catch (NumberFormatException exception) {
                throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT, exception.getMessage());
            }
            builder.setGiftId(StringValue.of(giftId));
        }

        if (Objects.nonNull(pricePerUnit)) {
            builder.setPricePerUnit(UInt64Value.of(pricePerUnit));
        }

        RetrieveSponsorResponse response;
        try {
            response = stub.updateSponsorByIslandId(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Update sponsor returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getSponsorMessage();
    }

    /**
     * 获取支持一下历史
     *
     * @param islandId 岛id
     * @return {@link RetrieveSponsorHistoryResponse}
     */
    public RetrieveSponsorHistoryResponse retrieveSponsorHistoryByIslandId(String islandId,
                                                                           int page,
                                                                           int pageSize) {

        SponsorHistoryServiceGrpc.SponsorHistoryServiceBlockingStub stub = SponsorHistoryServiceGrpc.newBlockingStub(this.vangaChannel);

        RetrieveSponsorHistoryRequest request = RetrieveSponsorHistoryRequest.newBuilder()
                .setIslandId(islandId)
                .setPageRequest(PageRequest.newBuilder().setPage(page).setPageSize(pageSize).build())
                .build();

        RetrieveSponsorHistoryResponse response;
        try {
            response = stub.retrieveSponsorHistoryByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve sponsor history returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

}
