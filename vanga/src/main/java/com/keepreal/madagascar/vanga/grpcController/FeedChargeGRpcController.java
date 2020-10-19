package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.FeedChargeServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveFeedChargeRequest;
import com.keepreal.madagascar.vanga.RetrieveFeedChargeResponse;
import com.keepreal.madagascar.vanga.RetrieveHasAccessFeedIdRequest;
import com.keepreal.madagascar.vanga.RetrieveHasAccessFeedIdResponse;
import com.keepreal.madagascar.vanga.model.FeedCharge;
import com.keepreal.madagascar.vanga.service.FeedChargeService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import java.util.List;

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

    @Override
    public void retrieveHasAccessFeedId(RetrieveHasAccessFeedIdRequest request, StreamObserver<RetrieveHasAccessFeedIdResponse> responseObserver) {
        String userId = request.getUserId();
        String islandId = request.getIslandId();

        List<String> feedIds;
        if (request.hasTimestampAfter()) {
            feedIds = this.feedChargeService.findHasAccessFeedIdTimestampAfter(userId, islandId, request.getTimestampAfter().getValue());
        } else {
            feedIds = this.feedChargeService.findHasAccessFeedIdTimestampBefore(userId, islandId, request.getTimestampAfter().getValue());
        }

        responseObserver.onNext(RetrieveHasAccessFeedIdResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllFeedIds(feedIds)
                .build());
        responseObserver.onCompleted();
    }
}
