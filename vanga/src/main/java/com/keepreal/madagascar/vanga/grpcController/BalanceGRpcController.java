package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.BalanceServiceGrpc;
import com.keepreal.madagascar.vanga.CreateBalanceByUserIdRequest;
import com.keepreal.madagascar.vanga.RetrieveBalanceByUserIdRequest;
import com.keepreal.madagascar.vanga.factory.BalanceMessageFactory;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.service.BalanceService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import java.util.Objects;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

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
     *
     * @param request          {@link RetrieveBalanceByUserIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveBalanceByUserId(RetrieveBalanceByUserIdRequest request,
                                        StreamObserver<BalanceResponse> responseObserver) {
        Balance balance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(request.getUserId());

        BalanceResponse response;
        if (Objects.nonNull(balance)) {
            response = BalanceResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setBalance(this.balanceMessageFactory.valueOf(balance))
                    .build();
        } else {
            response = BalanceResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_BALANCE_NOT_FOUND_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Creates balance for user.
     *
     * @param request {@link CreateBalanceByUserIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void createBalanceByUserId(CreateBalanceByUserIdRequest request,
                                     StreamObserver<CommonStatus> responseObserver) {
        this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(request.getUserId());

        CommonStatus response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
