package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.IslandResponse;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveIslandByIdRequest;
import com.keepreal.madagascar.fossa.CheckNewFeedsRequest;
import com.keepreal.madagascar.fossa.CheckNewFeedsResponse;
import com.keepreal.madagascar.fossa.DeleteFeedByIdRequest;
import com.keepreal.madagascar.fossa.DeleteFeedResponse;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.fossa.NewFeedsRequest;
import com.keepreal.madagascar.fossa.NewFeedsResponse;
import com.keepreal.madagascar.fossa.QueryFeedCondition;
import com.keepreal.madagascar.fossa.RetrieveFeedByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveMultipleFeedsRequest;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@GRpcService
public class FeedInfoService extends FeedServiceGrpc.FeedServiceImplBase {

    public static final int DEFAULT_LAST_COMMENT_COUNT = 5;

    private final ManagedChannel managedChannel;
    private final MongoTemplate mongoTemplate;
    private final CommentService commentService;

    @Autowired
    public FeedInfoService(MongoTemplate mongoTemplate, CommentService commentService, ManagedChannel managedChannel) {
        this.mongoTemplate = mongoTemplate;
        this.commentService = commentService;
        this.managedChannel = managedChannel;
    }

    /**
     * 创建一个feed
     * @param request
     * @param responseObserver
     */
    @Override
    public void createFeeds(NewFeedsRequest request, StreamObserver<NewFeedsResponse> responseObserver) {
        String userId = request.getUserId();
        List<String> islandIdList = request.getIslandIdList().stream().map(s -> toString()).collect(Collectors.toList());
        List<String> imageUrisList = request.getImageUrisList().stream().map(s -> toString()).collect(Collectors.toList());
        String text = request.hasText() ? request.getText().getValue() : "";
        islandIdList.forEach(id -> {
            // rpc调用coua服务，如果通过id拿不到island的信息，那么hostId就是""，fromHost=false
            String hostId = callCouaGetIslandHostId(id);
            FeedInfo feedInfo = new FeedInfo();
            feedInfo.setIslandId(Long.valueOf(id));
            feedInfo.setUserId(Long.valueOf(userId));
            feedInfo.setImageUrls(imageUrisList);
            feedInfo.setText(text);
            feedInfo.setRepostCount(0);
            feedInfo.setCommentsCount(0);
            feedInfo.setLikesCount(0);
            feedInfo.setDeleted(false);
            feedInfo.setFromHost(userId.equals(hostId));
            feedInfo.setCreatedTime(System.currentTimeMillis());
            feedInfo.setUpdatedTime(System.currentTimeMillis());
            mongoTemplate.save(feedInfo);
            //todo: 调用coua服务，更新island的lastFeedAt字段的值
        });

        NewFeedsResponse newFeedsResponse = NewFeedsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(newFeedsResponse);
        responseObserver.onCompleted();
    }

    /**
     * 删除一个feed
     * @param request
     * @param responseObserver
     */
    @Override
    public void deleteFeedById(DeleteFeedByIdRequest request, StreamObserver<DeleteFeedResponse> responseObserver) {
        String feedId = request.getId();
        mongoTemplate.remove(new Query(Criteria.where("id").is(feedId)), FeedInfo.class);

        DeleteFeedResponse deleteFeedResponse = DeleteFeedResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(deleteFeedResponse);
        responseObserver.onCompleted();
    }

    /**
     * 根据id返回一个feed信息
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveFeedById(RetrieveFeedByIdRequest request, StreamObserver<FeedResponse> responseObserver) {
        FeedResponse.Builder responseBuilder = FeedResponse.newBuilder();

        String feedId = request.getId();
        FeedInfo feedInfo = mongoTemplate.findById(feedId, FeedInfo.class);
        if (feedInfo != null) {
            FeedMessage feedMessage = getFeedMessage(feedInfo);
            responseBuilder.setFeed(feedMessage)
                    .setUserId(feedInfo.getUserId().toString())
                    .setStatus(CommonStatusUtils.getSuccStatus());
        } else {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEED_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * 根据条件返回多个feed信息
     * @param request 两个条件，islandId(string)和fromHost(boolean)
     * @param responseObserver
     */
    @Override
    public void retrieveMultipleFeeds(RetrieveMultipleFeedsRequest request, StreamObserver<FeedsResponse> responseObserver) {
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();
        QueryFeedCondition condition = request.getCondition();
        boolean fromHost = condition.hasFromHost();
        boolean hasIslandId = condition.hasIslandId();
        Query query = new Query();
        if (fromHost && hasIslandId) {
            Criteria criteria = Criteria
                    .where("fromHost").is(condition.getFromHost().getValue())
                    .and("islandId").is(condition.getIslandId().getValue());
            query.addCriteria(criteria);
        } else if (fromHost || hasIslandId) {
            Criteria criteria = fromHost ? Criteria.where("fromHost").is(condition.getFromHost().getValue())
                    : Criteria.where("islandId").is(condition.getIslandId().getValue());
            query.addCriteria(criteria);
        }

        long totalCount = mongoTemplate.count(query, FeedInfo.class);
        List<FeedInfo> feedInfoList = mongoTemplate.find(query.skip(page * pageSize).limit(pageSize), FeedInfo.class);
        List<FeedMessage> feedMessageList = feedInfoList.stream().map(this::getFeedMessage).collect(Collectors.toList());
        PageResponse pageResponse = PageResponse.newBuilder()
                .setPage(page)
                .setPageSize(pageSize)
                .setHasContent(feedInfoList.size() > 0)
                .setHasMore(page * pageSize < totalCount)
                .build();
        FeedsResponse feedsResponse = FeedsResponse.newBuilder()
                .addAllFeed(feedMessageList)
                .setPageResponse(pageResponse)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(feedsResponse);
        responseObserver.onCompleted();
    }

    /**
     * 根据岛的id和上次查看的时间，返回是否有新的feed
     * @param request
     * @param responseObserver
     */
    @Override
    public void checkNewFeeds(CheckNewFeedsRequest request, StreamObserver<CheckNewFeedsResponse> responseObserver) {
    }

    private FeedMessage getFeedMessage(FeedInfo feedInfo) {
        List<CommentMessage> lastCommentMessage = commentService
                .getLastCommentMessage(feedInfo.getId(), DEFAULT_LAST_COMMENT_COUNT);
        return FeedMessage.newBuilder()
                .setId(feedInfo.getId().toString())
                .setIslandId(feedInfo.getIslandId().toString())
                .setUserId(feedInfo.getUserId().toString())
                .setText(feedInfo.getText())
                .setFromHost(feedInfo.getFromHost())
                .addAllImageUris(feedInfo.getImageUrls())
                .setCreatedAt(feedInfo.getCreatedTime())
                .setCommentsCount(feedInfo.getCommentsCount())
                .setLikesCount(feedInfo.getLikesCount())
                .setRepostCount(feedInfo.getRepostCount())
                .addAllLastComments(lastCommentMessage)
                .build();
    }

    private String callCouaGetIslandHostId(String islandId) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);
        RetrieveIslandByIdRequest islandByIdRequest = RetrieveIslandByIdRequest.newBuilder().setId(islandId).build();
        IslandResponse islandResponse = stub.retrieveIslandById(islandByIdRequest);
        return islandResponse.hasIsland() ? islandResponse.getIsland().getHostId() : "";
    }
}
