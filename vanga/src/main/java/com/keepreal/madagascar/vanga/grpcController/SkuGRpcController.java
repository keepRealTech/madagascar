package com.keepreal.madagascar.vanga.grpcController;

import com.google.protobuf.UInt64Value;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.CreateMembershipSkusRequest;
import com.keepreal.madagascar.vanga.CreateSponsorSkusRequest;
import com.keepreal.madagascar.vanga.DeleteMembershipSkusByIdRequest;
import com.keepreal.madagascar.vanga.MembershipSkusResponse;
import com.keepreal.madagascar.vanga.RetrieveMembershipSkusByMembershipIdRequest;
import com.keepreal.madagascar.vanga.RetrieveShellSkusRequest;
import com.keepreal.madagascar.vanga.RetrieveSponsorSkusRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportSkusRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportSkusResponse;
import com.keepreal.madagascar.vanga.ShellSkusResponse;
import com.keepreal.madagascar.vanga.SkuServiceGrpc;
import com.keepreal.madagascar.vanga.SponsorSkusResponse;
import com.keepreal.madagascar.vanga.UpdateMembershipSkusByIdRequest;
import com.keepreal.madagascar.vanga.UpdateSponsorSkusRequest;
import com.keepreal.madagascar.vanga.factory.SkuMessageFactory;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.model.SponsorSku;
import com.keepreal.madagascar.vanga.model.SupportSku;
import com.keepreal.madagascar.vanga.service.SkuService;
import com.keepreal.madagascar.vanga.service.SponsorHistoryService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the sku grpc controller.
 */
@GRpcService
public class SkuGRpcController extends SkuServiceGrpc.SkuServiceImplBase {

    private final SkuService skuService;
    private final SkuMessageFactory skuMessageFactory;
    private final SponsorHistoryService sponsorHistoryService;

    /**
     * Constructs the sku grpc controller.
     *
     * @param skuService        {@link SkuService}.
     * @param skuMessageFactory {@link SkuMessageFactory}.
     * @param sponsorHistoryService {@link SponsorHistoryService}
     */
    public SkuGRpcController(SkuService skuService,
                             SkuMessageFactory skuMessageFactory,
                             SponsorHistoryService sponsorHistoryService) {
        this.skuService = skuService;
        this.skuMessageFactory = skuMessageFactory;
        this.sponsorHistoryService = sponsorHistoryService;
    }

    /**
     * Implements retrieving all active shell skus.
     *
     * @param request          {@link RetrieveShellSkusRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveActiveShellSkus(RetrieveShellSkusRequest request,
                                        StreamObserver<ShellSkusResponse> responseObserver) {
        List<ShellSku> shellSkus = this.skuService.retrieveShellSkusByActiveIsTrue(request.getIsWechatPay());

        ShellSkusResponse response = ShellSkusResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllShellSkus(shellSkus.stream()
                        .map(this.skuMessageFactory::valueOf)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements retrieving all active membership skus for a given membership id.
     *
     * @param request          {@link RetrieveMembershipSkusByMembershipIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveActiveMembershipSkusByMembershipId(RetrieveMembershipSkusByMembershipIdRequest request,
                                                           StreamObserver<MembershipSkusResponse> responseObserver) {
        List<MembershipSku> membershipSkus = this.skuService.retrieveMembershipSkusByMembershipIdAndActiveIsTrue(request.getMembershipId());

        MembershipSkusResponse response = MembershipSkusResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllMembershipSkus(membershipSkus.stream()
                        .map(this.skuMessageFactory::valueOf)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the creation of membership skus for a given membership.
     *
     * @param request          {@link CreateMembershipSkusRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void createMembershipSkusByMembershipId(CreateMembershipSkusRequest request,
                                                   StreamObserver<MembershipSkusResponse> responseObserver) {
        List<MembershipSku> membershipSkus = this.skuService
                .createDefaultMembershipSkusByMembershipIdAndCostPerMonth(request.getMembershipId(),
                        request.getMembershipName(),
                        request.getHostId(),
                        request.getIslandId(),
                        request.getPriceInCentsPerMonth(),
                        request.getPermanent());

        MembershipSkusResponse response = MembershipSkusResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllMembershipSkus(membershipSkus.stream()
                        .map(this.skuMessageFactory::valueOf)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the delete membership skus by membership id.
     *
     * @param request          {@link DeleteMembershipSkusByIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void deleteMembershipSkusByMembershipId(DeleteMembershipSkusByIdRequest request,
                                                   StreamObserver<CommonStatus> responseObserver) {
        this.skuService.deleteMembershipSkusByMembershipId(request.getMembershipId());

        CommonStatus response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Updates the membership skus by membership id.
     *
     * @param request {@link UpdateMembershipSkusByIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void updateMembershipSkusByMembershipId(UpdateMembershipSkusByIdRequest request,
                                                   StreamObserver<MembershipSkusResponse> responseObserver) {
        List<MembershipSku> membershipSkus = this.skuService.retrieveMembershipSkusByMembershipId(request.getMembershipId());

        if (request.hasPermanent() && request.getPermanent().getValue()) {
            membershipSkus = this.skuService.obsoleteMembershipSkusWithPermanent(request, membershipSkus);
        } else {
            if (request.hasPricePerMonth()) {
                membershipSkus = this.skuService.obsoleteMembershipSkusWithNewPrice(request.getMembershipId(),
                        membershipSkus, request.getPricePerMonth().getValue());
            }

            if (request.hasMembershipName()) {
                membershipSkus = membershipSkus.stream()
                        .peek(membershipSku -> membershipSku.setMembershipName(request.getMembershipName().getValue()))
                        .collect(Collectors.toList());
            }

            if (request.hasActive()) {
                membershipSkus = membershipSkus.stream()
                        .peek(membershipSku -> membershipSku.setActive(request.getActive().getValue()))
                        .collect(Collectors.toList());
            }
        }

        List<MembershipSku> membershipSkuList = this.skuService.updateAll(membershipSkus);
        MembershipSkusResponse response = MembershipSkusResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllMembershipSkus(membershipSkuList.stream()
                        .map(this.skuMessageFactory::valueOf)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveActiveSupportSkus(RetrieveSupportSkusRequest request, StreamObserver<RetrieveSupportSkusResponse> responseObserver) {
        List<SupportSku> supportSkus = this.skuService.retrieveSupportSkus();

        responseObserver.onNext(RetrieveSupportSkusResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllSupportSkus(supportSkus.stream().map(this.skuMessageFactory::valueOf).collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * 创建支持一下sku
     *
     * @param request {@link CreateSponsorSkusRequest}
     * @param responseObserver {@link SponsorSkusResponse}
     */
    @Override
    public void createSponsorSkusBySponsorId(CreateSponsorSkusRequest request, StreamObserver<SponsorSkusResponse> responseObserver) {
        List<SponsorSku> sponsorSkus = this.skuService.createDefaultSponsorSkusBySponsorIdAndPricePerUnit(request.getSponsorId(),
                        request.getHostId(),
                        request.getIslandId(),
                        request.getPriceInCentsPerUnit(),
                        request.getGiftId());

        SponsorSkusResponse response = SponsorSkusResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllSponsorSkus(sponsorSkus.stream()
                    .map(this.skuMessageFactory::valueOf)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 更新支持一下sku
     *
     * @param request {@link UpdateSponsorSkusRequest}
     * @param responseObserver {@link SponsorSkusResponse}
     */
    @Override
    public void updateSponsorSkusBySponsorId(UpdateSponsorSkusRequest request, StreamObserver<SponsorSkusResponse> responseObserver) {
        List<SponsorSku> sponsorSkus = this.skuService.retrieveSponsorSkusBySponsorId(request.getSponsorId());

        if (request.hasPriceInCentsPerUnit()) {
            sponsorSkus = this.skuService.obsoleteSponsorSkusWithNewUnitPrice(request.getSponsorId(),
                    sponsorSkus, request.getPriceInCentsPerUnit().getValue());
        }

        if (request.hasGiftId()) {
            sponsorSkus = sponsorSkus.stream()
                    .peek(sponsorSku -> sponsorSku.setGiftId(request.getGiftId().getValue()))
                    .collect(Collectors.toList());
        }

        sponsorSkus = this.skuService.updateAllSponsorSkus(sponsorSkus);
        SponsorSkusResponse response = SponsorSkusResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllSponsorSkus(sponsorSkus.stream()
                        .map(this.skuMessageFactory::valueOf)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 获取支持一下 sku
     *
     * @param request {@link RetrieveSponsorSkusRequest}
     * @param responseObserver {@link SponsorSkusResponse}
     */
    @Override
    public void retrieveSponsorSkusByIslandId(RetrieveSponsorSkusRequest request, StreamObserver<SponsorSkusResponse> responseObserver) {
        List<SponsorSku> sponsorSkus = this.skuService.retrieveSponsorSkusByIslandId(request.getIslandId());
        SponsorSkusResponse.Builder builder = SponsorSkusResponse.newBuilder();
        if (sponsorSkus.isEmpty()) {
            sponsorSkus = this.skuService.retrieveSponsorSkusByIslandId(Constants.DEFAULT_SPONSOR_SKU_ISLAND_ID);
        }

        responseObserver.onNext(builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllSponsorSkus(sponsorSkus.stream().map(this.skuMessageFactory::valueOf).collect(Collectors.toList()))
                .setCount(this.sponsorHistoryService.retrieveSponsorHistoryCountByIslandId(request.getIslandId()))
                .build());
        responseObserver.onCompleted();
    }
}
