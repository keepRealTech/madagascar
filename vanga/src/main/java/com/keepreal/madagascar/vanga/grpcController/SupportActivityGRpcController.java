package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.RetrieveActivityBonusRequest;
import com.keepreal.madagascar.vanga.RetrieveActivityBonusResponse;
import com.keepreal.madagascar.vanga.SupportActivityGrpc;
import com.keepreal.madagascar.vanga.service.SupportActivityService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class SupportActivityGRpcController extends SupportActivityGrpc.SupportActivityImplBase {

    private final SupportActivityService supportActivityService;

    public SupportActivityGRpcController(SupportActivityService supportActivityService) {
        this.supportActivityService = supportActivityService;
    }

    @Override
    public void retrieveActivityBonus(RetrieveActivityBonusRequest request, StreamObserver<RetrieveActivityBonusResponse> responseObserver) {
        String userId = request.getUserId();

        responseObserver.onNext(RetrieveActivityBonusResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setMessage(this.supportActivityService.getActivityInfo(userId))
                .build());
        responseObserver.onCompleted();
    }
}
