package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.IslandResponse;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveIslandByIdRequest;
import com.keepreal.madagascar.coua.UpdateLastFeedAtRequest;
import com.keepreal.madagascar.coua.UpdateLastFeedAtResponse;
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
import com.keepreal.madagascar.fossa.dao.FeedInfoRepository;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final FeedInfoRepository feedInfoRepository;
    private final LongIdGenerator idGenerator;

    @Autowired
    public FeedInfoService(MongoTemplate mongoTemplate, CommentService commentService, ManagedChannel managedChannel, FeedInfoRepository feedInfoRepository, LongIdGenerator idGenerator) {
        this.mongoTemplate = mongoTemplate;
        this.commentService = commentService;
        this.managedChannel = managedChannel;
        this.feedInfoRepository = feedInfoRepository;
        this.idGenerator = idGenerator;
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
        List<FeedInfo> feedInfoList = new ArrayList<>();
        islandIdList.forEach(id -> {
            // rpc调用coua服务，如果通过id拿不到island的信息，那么hostId就是""，fromHost=false
            String hostId = callCouaGetIslandHostId(id);
            FeedInfo feedInfo = new FeedInfo();
            feedInfo.setId(idGenerator.nextId());
            feedInfo.setIslandId(Long.valueOf(id));
            feedInfo.setUserId(Long.valueOf(userId));
            feedInfo.setImageUrls(imageUrisList);
            feedInfo.setText(text);
            feedInfo.setRepostCount(0);
            feedInfo.setCommentsCount(0);
            feedInfo.setLikesCount(0);
            feedInfo.setDeleted(false);
            feedInfo.setFromHost(userId.equals(hostId));
            feedInfoList.add(feedInfo);
        });
        //调用coua服务更新island的lastFeedAt字段
        callCouaUpdateIslandLastFeedAt(islandIdList);
        feedInfoRepository.saveAll(feedInfoList);
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

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(feedId)),
                Update.update("deleted", true),
                FeedInfo.class);

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
        FeedInfo feedInfo = feedInfoRepository.findFeedInfoByIdAndDeletedIsFalse(Long.valueOf(feedId));
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
        query.addCriteria(Criteria.where("deleted").is(false));
        if (fromHost && hasIslandId) { //两个条件都存在
            Criteria criteria = Criteria
                    .where("fromHost").is(condition.getFromHost().getValue())
                    .and("islandId").is(condition.getIslandId().getValue());
            query.addCriteria(criteria);
        } else if (fromHost || hasIslandId) { //只有一个条件
            Criteria criteria = fromHost ? Criteria.where("fromHost").is(condition.getFromHost().getValue())
                    : Criteria.where("islandId").is(condition.getIslandId().getValue());
            query.addCriteria(criteria);
        }

        // 没有条件
        long totalCount = mongoTemplate.count(query, FeedInfo.class);
        List<FeedInfo> feedInfoList = mongoTemplate.find(query.with(PageRequest.of(page, pageSize)), FeedInfo.class);
        List<FeedMessage> feedMessageList = feedInfoList.stream().map(this::getFeedMessage).collect(Collectors.toList());
        PageResponse pageResponse = PageRequestResponseUtils.buildPageResponse(page, pageSize, totalCount);
        FeedsResponse feedsResponse = FeedsResponse.newBuilder()
                .addAllFeed(feedMessageList)
                .setPageResponse(pageResponse)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(feedsResponse);
        responseObserver.onCompleted();
    }

    public FeedMessage getFeedMessageById(Long feedId) {
        Optional<FeedInfo> feedInfoOptional = feedInfoRepository.findById(feedId);
        if (feedInfoOptional.isPresent()) {
            return getFeedMessage(feedInfoOptional.get());
        } else {
            return FeedMessage.newBuilder().build();
        }
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

    private void callCouaUpdateIslandLastFeedAt(List<String> islandIdList) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);
        UpdateLastFeedAtRequest request = UpdateLastFeedAtRequest.newBuilder()
                .addAllIslandIds(islandIdList)
                .setTimestamps(System.currentTimeMillis())
                .build();
        stub.updateLastFeedAtById(request);
    }
}
