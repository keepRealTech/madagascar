package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.fossa.DeleteFeedByIdRequest;
import com.keepreal.madagascar.fossa.DeleteFeedResponse;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.fossa.NewFeedRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveMultipleFeedsRequest;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@GRpcService
public class FeedInfoService extends FeedServiceGrpc.FeedServiceImplBase {

    @Override
    public void createFeed(NewFeedRequest request, StreamObserver<FeedResponse> responseObserver) {
        super.createFeed(request, responseObserver);
    }

    @Override
    public void deleteFeedNyId(DeleteFeedByIdRequest request, StreamObserver<DeleteFeedResponse> responseObserver) {
        super.deleteFeedNyId(request, responseObserver);
    }

    @Override
    public void retrieveFeedById(RetrieveFeedByIdRequest request, StreamObserver<FeedResponse> responseObserver) {
        super.retrieveFeedById(request, responseObserver);
    }

    @Override
    public void retrieveMultipleFeeds(RetrieveMultipleFeedsRequest request, StreamObserver<FeedsResponse> responseObserver) {
        super.retrieveMultipleFeeds(request, responseObserver);
    }
}
