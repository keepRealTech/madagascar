package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.CreateWithdrawRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.factory.BalanceMessageFactory;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.service.PaymentService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;


/**
 * Represents the payment grpc controller.
 */
@GRpcService
public class PaymentGRpcController extends PaymentServiceGrpc.PaymentServiceImplBase {

    private final PaymentService paymentService;
    private final BalanceMessageFactory balanceMessageFactory;

    public PaymentGRpcController(PaymentService paymentService,
                                 BalanceMessageFactory balanceMessageFactory) {
        this.paymentService = paymentService;
        this.balanceMessageFactory = balanceMessageFactory;
    }

    /**
     * Implements the create withdraw request.
     *
     * @param request          {@link CreateWithdrawRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    public void createWithdrawPayment(CreateWithdrawRequest request,
                                      StreamObserver<BalanceResponse> responseObserver) {
        BalanceResponse response = null;
        try {
            Balance balance = this.paymentService.createWithdrawPayment(request.getUserId(), request.getWithdrawAmountInCents());
            response = BalanceResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setBalance(this.balanceMessageFactory.valueOf(balance))
                    .build();
        } catch (KeepRealBusinessException exception) {
            response = BalanceResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(exception.getErrorCode()))
                    .build();
        } finally {
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

}
