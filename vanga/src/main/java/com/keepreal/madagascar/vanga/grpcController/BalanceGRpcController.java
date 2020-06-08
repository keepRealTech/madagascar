package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.BalanceServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveBalanceByUserIdRequest;
import com.keepreal.madagascar.vanga.factory.BalanceMessageFactory;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.service.BalanceService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * Represents the balance grpc controller.
 */
@GRpcService
public class BalanceGRpcController extends BalanceServiceGrpc.BalanceServiceImplBase {

    private final BalanceService balanceService;
    private final BalanceMessageFactory balanceMessageFactory;

    /**
     * Constructs the balance grpc controller.
     *
     * @param balanceService        {@link BalanceService}.
     * @param balanceMessageFactory {@link BalanceMessageFactory}.
     */
    public BalanceGRpcController(BalanceService balanceService, BalanceMessageFactory balanceMessageFactory) {
        this.balanceService = balanceService;
        this.balanceMessageFactory = balanceMessageFactory;
    }

    /**
     * Retrieves balance by user id.
     */
    public void retrieveBalanceByUserId(RetrieveBalanceByUserIdRequest request,
                                        StreamObserver<BalanceResponse> responseObserver) {
        String userId = request.getUserId();
        Balance balance = this.balanceService.retrieveOrCreateIfNotExistsByUserId(userId);

        BalanceResponse response = BalanceResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setBalance(this.balanceMessageFactory.valueOf(balance))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
