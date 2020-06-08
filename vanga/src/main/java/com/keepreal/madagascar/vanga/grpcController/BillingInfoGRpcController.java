package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.BillingInfoResponse;
import com.keepreal.madagascar.vanga.BillingInfoServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveBillingInfoByUserIdRequest;
import com.keepreal.madagascar.vanga.UpdateBillingInfoByUserIdRequest;
import com.keepreal.madagascar.vanga.factory.BillingInfoMessageFactory;
import com.keepreal.madagascar.vanga.model.BillingInfo;
import com.keepreal.madagascar.vanga.service.BillingInfoService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * Represents the billing info grpc controller.
 */
@GRpcService
public class BillingInfoGRpcController extends BillingInfoServiceGrpc.BillingInfoServiceImplBase {

    private final BillingInfoService billingInfoService;
    private final BillingInfoMessageFactory billingInfoMessageFactory;

    /**
     * Constructs the billing info grpc controller.
     *
     * @param billingInfoService        {@link BillingInfoService}.
     * @param billingInfoMessageFactory {@link BillingInfoMessageFactory}.
     */
    public BillingInfoGRpcController(BillingInfoService billingInfoService, BillingInfoMessageFactory billingInfoMessageFactory) {
        this.billingInfoService = billingInfoService;
        this.billingInfoMessageFactory = billingInfoMessageFactory;
    }

    /**
     * Retrieves billing info by user id.
     */
    @Override
    public void retrieveBillingInfoByUserId(RetrieveBillingInfoByUserIdRequest request,
                                            StreamObserver<BillingInfoResponse> responseObserver) {
        BillingInfo billingInfo = this.billingInfoService.retrieveOrCreateBillingInfoIfNotExistsByUserId(request.getUserId());

        BillingInfoResponse response = BillingInfoResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setBillingInfo(this.billingInfoMessageFactory.valueOf(billingInfo))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Updates the billing info for given user id.
     */
    @Override
    public void updateBillingInfoByUserId(UpdateBillingInfoByUserIdRequest request,
                                          StreamObserver<BillingInfoResponse> responseObserver) {
        BillingInfo billingInfo = this.billingInfoService.updateBillingInfoByUserId(request.getUserId(),
                request.getName(), request.getMobile(), request.getAccountNumber(), request.getIdNumber());

        BillingInfoResponse response = BillingInfoResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setBillingInfo(this.billingInfoMessageFactory.valueOf(billingInfo))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
