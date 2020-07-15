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

import java.util.Objects;

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
     *
     * @param request          {@link RetrieveBillingInfoByUserIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     **/
    @Override
    public void retrieveBillingInfoByUserId(RetrieveBillingInfoByUserIdRequest request,
                                            StreamObserver<BillingInfoResponse> responseObserver) {
        BillingInfo billingInfo = this.billingInfoService.retrieveOrCreateBillingInfoIfNotExistsByUserId(request.getUserId());

        BillingInfoResponse response;
        if (Objects.nonNull(billingInfo)) {
            response = BillingInfoResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setBillingInfo(this.billingInfoMessageFactory.valueOf(billingInfo))
                    .build();
        } else {
            response = BillingInfoResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_BILLING_INFO_NOT_FOUND_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Updates the billing info for given user id.
     *
     * @param request          {@link UpdateBillingInfoByUserIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void updateBillingInfoByUserId(UpdateBillingInfoByUserIdRequest request,
                                          StreamObserver<BillingInfoResponse> responseObserver) {
        BillingInfo billingInfo = this.billingInfoService.updateBillingInfoByUserId(request.getUserId(),
                request.hasName() ? request.getName().getValue() : null,
                request.hasMobile() ? request.getMobile().getValue() : null,
                request.hasAccountNumber() ? request.getAccountNumber().getValue() : null,
                request.hasIdNumber() ? request.getIdNumber().getValue() : null,
                request.hasIdFrontUrl() ? request.getIdFrontUrl().getValue() : null,
                request.hasIdBackUrl() ? request.getIdBackUrl().getValue() : null);

        BillingInfoResponse response;
        if (Objects.nonNull(billingInfo)) {
            response = BillingInfoResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setBillingInfo(this.billingInfoMessageFactory.valueOf(billingInfo))
                    .build();
        } else {
            response = BillingInfoResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_BILLING_INFO_NOT_FOUND_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
