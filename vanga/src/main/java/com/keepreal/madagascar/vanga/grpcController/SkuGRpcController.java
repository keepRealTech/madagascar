package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.BillingInfoResponse;
import com.keepreal.madagascar.vanga.MembershipSkusResponse;
import com.keepreal.madagascar.vanga.RetrieveMembershipSkusByMembershipIdRequest;
import com.keepreal.madagascar.vanga.RetrieveShellSkusRequest;
import com.keepreal.madagascar.vanga.ShellSkusResponse;
import com.keepreal.madagascar.vanga.SkuServiceGrpc;
import com.keepreal.madagascar.vanga.factory.SkuMessageFactory;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.service.SkuService;
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

}
