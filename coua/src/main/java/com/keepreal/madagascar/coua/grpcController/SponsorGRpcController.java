package com.keepreal.madagascar.coua.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.RetrieveSingleSponsorGiftRequest;
import com.keepreal.madagascar.coua.RetrieveSingleSponsorGiftResponse;
import com.keepreal.madagascar.coua.RetrieveSponsorGiftsRequest;
import com.keepreal.madagascar.coua.RetrieveSponsorGiftsResponse;
import com.keepreal.madagascar.coua.RetrieveSponsorRequest;
import com.keepreal.madagascar.coua.RetrieveSponsorResponse;
import com.keepreal.madagascar.coua.SponsorServiceGrpc;
import com.keepreal.madagascar.coua.UpdateSponsorByIslandIdRequest;
import com.keepreal.madagascar.coua.model.IslandInfo;
import com.keepreal.madagascar.coua.model.Sponsor;
import com.keepreal.madagascar.coua.model.SponsorGift;
import com.keepreal.madagascar.coua.service.IslandInfoService;
import com.keepreal.madagascar.coua.service.SkuService;
import com.keepreal.madagascar.coua.service.SponsorService;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the sponsor grpc controller.
 */
@GRpcService
@Slf4j
public class SponsorGRpcController extends SponsorServiceGrpc.SponsorServiceImplBase {

    private final SponsorService sponsorService;
    private final IslandInfoService islandInfoService;
    private final SkuService skuService;

    /**
     * Constructor the sponsor grpc controller.
     *
     * @param sponsorService {@link SponsorService}
     * @param islandInfoService {@link IslandInfoService}
     * @param skuService {@link SkuService}
     */
    public SponsorGRpcController(SponsorService sponsorService,
                                 IslandInfoService islandInfoService,
                                 SkuService skuService) {
        this.sponsorService = sponsorService;
        this.islandInfoService = islandInfoService;
        this.skuService = skuService;
    }

    /**
     * retrieve sponsor by condition
     *
     * @param request {@link RetrieveSponsorRequest}
     * @param responseObserver {@link RetrieveSponsorResponse}
     */
    @Override
    public void retrieveSponsor(RetrieveSponsorRequest request, StreamObserver<RetrieveSponsorResponse> responseObserver) {
        RetrieveSponsorResponse.Builder builder = RetrieveSponsorResponse.newBuilder();
        String islandId = request.getIslandId();
        Sponsor sponsor;
        sponsor = this.sponsorService.retrieveSponsorByIslandId(islandId);
        if (Objects.isNull(sponsor)) {
            IslandInfo islandInfo = this.islandInfoService.findTopByIdAndDeletedIsFalse(islandId);
            if (Objects.isNull(islandInfo)) {
                log.error("retrieve island return null islandId is {}", islandId);
                builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NOT_FOUND_ERROR));
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            } else {
                sponsor = this.sponsorService.createDefaultSponsor(islandInfo.getId(), islandInfo.getHostId());
                this.skuService.createSponsorSkusBySponsorId(sponsor.getId(),
                        sponsor.getPricePerUnit(),
                        sponsor.getHostId(),
                        sponsor.getIslandId(),
                        sponsor.getGiftId());
            }
        }
        responseObserver.onNext(builder.setStatus(CommonStatusUtils.getSuccStatus()).
                setSponsorMessage(this.sponsorService.getSponsorMessage(sponsor))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * 获取支持一下礼物表情
     *
     * @param request {@link RetrieveSponsorGiftsRequest}
     * @param responseObserver {@link RetrieveSponsorGiftsResponse}
     */
    @Override
    public void retrieveSponsorGifts(RetrieveSponsorGiftsRequest request, StreamObserver<RetrieveSponsorGiftsResponse> responseObserver) {
        List<SponsorGift> sponsorGifts = this.sponsorService.retrieveSponsorGiftByCondition(request.getOnlyDefault());
        RetrieveSponsorGiftsResponse response = RetrieveSponsorGiftsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllSponsorGifts(sponsorGifts.stream()
                        .map(this.sponsorService::getSponsorGiftMessage)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 更新支持一下信息
     *
     * @param request {@link UpdateSponsorByIslandIdRequest}
     * @param responseObserver {@link RetrieveSponsorResponse}
     */
    @Override
    public void updateSponsorByIslandId(UpdateSponsorByIslandIdRequest request, StreamObserver<RetrieveSponsorResponse> responseObserver) {
        RetrieveSponsorResponse.Builder builder = RetrieveSponsorResponse.newBuilder();
        String islandId = request.getIslandId();
        Sponsor sponsor = this.sponsorService.retrieveSponsorByIslandId(islandId);
        if (Objects.isNull(sponsor)) {
            responseObserver.onNext(builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SPONSOR_NOT_FOUND_ERROR)).build());
            responseObserver.onCompleted();
        }

        if (request.hasPricePerUnit() || request.hasGiftId()) {
           sponsor = this.sponsorService.updateSponsorAndSku(sponsor, request);
        } else {
            if (request.hasDescription()) {
                sponsor.setDescription(request.getDescription().getValue());
            }
            sponsor = this.sponsorService.updateSponsor(sponsor);
        }

        responseObserver.onNext(builder
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setSponsorMessage(this.sponsorService.getSponsorMessage(sponsor))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * 获取单个支持一下礼物信息
     *
     * @param request {@link RetrieveSingleSponsorGiftRequest}
     * @param responseObserver {@link RetrieveSingleSponsorGiftResponse}
     */
    @Override
    public void retrieveSingleSponsorGift(RetrieveSingleSponsorGiftRequest request, StreamObserver<RetrieveSingleSponsorGiftResponse> responseObserver) {
        RetrieveSingleSponsorGiftResponse.Builder builder = RetrieveSingleSponsorGiftResponse.newBuilder();
        SponsorGift sponsorGift = this.sponsorService.retrieveSponsorGiftById(request.getGiftId());
        if (Objects.isNull(sponsorGift)) {
            responseObserver.onNext(builder
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SPONSOR_GIFT_NOT_FOUND_ERROR))
                    .build());
            responseObserver.onCompleted();
        }
        responseObserver.onNext(builder
                .setSponsorGift(this.sponsorService.getSponsorGiftMessage(sponsorGift))
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build());
        responseObserver.onCompleted();
    }
}
