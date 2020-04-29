package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.fossa.CheckNewFeedsRequest;
import com.keepreal.madagascar.fossa.CheckNewFeedsResponse;
import com.keepreal.madagascar.fossa.DeleteFeedByIdRequest;
import com.keepreal.madagascar.fossa.DeleteFeedResponse;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.fossa.NewFeedsRequest;
import com.keepreal.madagascar.fossa.NewFeedsResponse;
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

    /**
     * 创建一个feed
     * @param request
     * @param responseObserver
     */
    @Override
    public void createFeeds(NewFeedsRequest request, StreamObserver<NewFeedsResponse> responseObserver) {
        super.createFeeds(request, responseObserver);
    }

    /**
     * 删除一个feed
     * @param request
     * @param responseObserver
     */
    @Override
    public void deleteFeedById(DeleteFeedByIdRequest request, StreamObserver<DeleteFeedResponse> responseObserver) {
        super.deleteFeedById(request, responseObserver);
    }

    /**
     * 根据id返回一个feed信息
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveFeedById(RetrieveFeedByIdRequest request, StreamObserver<FeedResponse> responseObserver) {
        super.retrieveFeedById(request, responseObserver);
    }

    /**
     * 根据条件返回多个feed信息
     * @param request 两个条件，islandId(string)和fromHost(boolean)
     * @param responseObserver
     */
    @Override
    public void retrieveMultipleFeeds(RetrieveMultipleFeedsRequest request, StreamObserver<FeedsResponse> responseObserver) {
        super.retrieveMultipleFeeds(request, responseObserver);
    }

    /**
     * 根据岛的id和上次查看的时间，返回是否有新的feed
     * @param request
     * @param responseObserver
     */
    @Override
    public void checkNewFeeds(CheckNewFeedsRequest request, StreamObserver<CheckNewFeedsResponse> responseObserver) {
        super.checkNewFeeds(request, responseObserver);
    }
}
