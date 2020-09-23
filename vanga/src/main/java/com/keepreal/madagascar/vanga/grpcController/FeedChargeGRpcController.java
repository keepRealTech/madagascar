package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.FeedChargeServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveFeedChargeRequest;
import com.keepreal.madagascar.vanga.RetrieveFeedChargeResponse;
import com.keepreal.madagascar.vanga.model.FeedCharge;
import com.keepreal.madagascar.vanga.service.FeedChargeService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class FeedChargeGRpcController extends FeedChargeServiceGrpc.FeedChargeServiceImplBase {

    private final FeedChargeService feedChargeService;

    public FeedChargeGRpcController(FeedChargeService feedChargeService) {
        this.feedChargeService = feedChargeService;
    }

    @Override
    public void retrieveFeedChargeAccess(RetrieveFeedChargeRequest request, StreamObserver<RetrieveFeedChargeResponse> responseObserver) {

        FeedCharge feedCharge = this.feedChargeService.findFeedCharge(request.getUserId(), request.getFeedId());

        responseObserver.onNext(RetrieveFeedChargeResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setHasAccess(feedCharge != null)
                .build());
        responseObserver.onCompleted();
    }
}
