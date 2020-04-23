package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.fossa.*;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import org.lognet.springboot.grpc.GRpcService;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-23
 **/

@GRpcService
public class FeedService extends FeedServiceGrpc.FeedServiceImplBase {

    @Override
    public void createFeed(NewFeedRequest request, io.grpc.stub.StreamObserver<FeedResponse> responseObserver) {
        FeedInfo feedInfo = new FeedInfo();
        super.createFeed(request, responseObserver);
    }

    @Override
    public void deleteFeed(DeleteFeedByIdRequest request, io.grpc.stub.StreamObserver<DeleteFeedResponse> responseObserver) {
        super.deleteFeed(request, responseObserver);
    }

    @Override
    public void retrieveFeedById(RetrieveFeedByIdRequest request, io.grpc.stub.StreamObserver<FeedResponse> responseObserver) {
        super.retrieveFeedById(request, responseObserver);
    }

    @Override
    public void retrieveFeeds(RetrieveFeedsByConditionRequest request, io.grpc.stub.StreamObserver<RetrieveFeedsResponse> responseObserver) {
        super.retrieveFeeds(request, responseObserver);
    }
}
