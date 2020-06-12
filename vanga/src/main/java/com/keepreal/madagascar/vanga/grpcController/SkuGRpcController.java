package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.CreateMembershipSkusRequest;
import com.keepreal.madagascar.vanga.DeleteMembershipSkusByIdRequest;
import com.keepreal.madagascar.vanga.MembershipSkusResponse;
import com.keepreal.madagascar.vanga.RetrieveMembershipSkusByMembershipIdRequest;
import com.keepreal.madagascar.vanga.RetrieveShellSkusRequest;
import com.keepreal.madagascar.vanga.ShellSkusResponse;
import com.keepreal.madagascar.vanga.SkuServiceGrpc;
import com.keepreal.madagascar.vanga.UpdateMembershipSkusByIdRequest;
import com.keepreal.madagascar.vanga.factory.SkuMessageFactory;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.service.SkuService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * Represents the sku grpc controller.
 */
@GRpcService
public class SkuGRpcController extends SkuServiceGrpc.SkuServiceImplBase {

    private final SkuService skuService;
    private final SkuMessageFactory skuMessageFactory;

    /**
     * Constructs the sku grpc controller.
     *
     * @param skuService        {@link SkuService}.
     * @param skuMessageFactory {@link SkuMessageFactory}.
     */
    public SkuGRpcController(SkuService skuService,
                             SkuMessageFactory skuMessageFactory) {
        this.skuService = skuService;
        this.skuMessageFactory = skuMessageFactory;
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
        List<ShellSku> shellSkus = this.skuService.retrieveShellSkusByActiveIsTrue();

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
    public void createMembershipSkusByMembershipId(CreateMembershipSkusRequest request,
                                                   StreamObserver<MembershipSkusResponse> responseObserver) {
        List<MembershipSku> membershipSkus = this.skuService
                .createDefaultMembershipSkusByMembershipIdAndCostPerMonth(request.getMembershipId(),
                        request.getMembershipName(),
                        request.getHostId(),
                        request.getIslandId(),
                        request.getPriceInCentsPerMonth());

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
    @Transactional
    public void updateMembershipSkusByMembershipId(UpdateMembershipSkusByIdRequest request,
                                                   StreamObserver<MembershipSkusResponse> responseObserver) {
        List<MembershipSku> membershipSkus = this.skuService.retrieveMembershipSkusByMembershipId(request.getMembershipId());
        if (request.hasPricePerMonth()) {
            membershipSkus = this.skuService.obsoleteMembershipSkusWithNewPrice(membershipSkus, request.getPricePerMonth().getValue());
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

}
