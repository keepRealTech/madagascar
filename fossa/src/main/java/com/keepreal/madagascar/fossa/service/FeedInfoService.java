package com.keepreal.madagascar.fossa.service;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.UpdateLastFeedAtRequest;
import com.keepreal.madagascar.fossa.CreateDefaultFeedRequest;
import com.keepreal.madagascar.fossa.CreateDefaultFeedResponse;
import com.keepreal.madagascar.fossa.DeleteFeedByIdRequest;
import com.keepreal.madagascar.fossa.DeleteFeedResponse;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.fossa.NewFeedsRequest;
import com.keepreal.madagascar.fossa.NewFeedsResponse;
import com.keepreal.madagascar.fossa.QueryFeedCondition;
import com.keepreal.madagascar.fossa.RetrieveFeedByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveLatestFeedByUserIdRequest;
import com.keepreal.madagascar.fossa.RetrieveMultipleFeedsRequest;
import com.keepreal.madagascar.fossa.dao.CommentInfoRepository;
import com.keepreal.madagascar.fossa.dao.FeedInfoRepository;
import com.keepreal.madagascar.fossa.model.CommentInfo;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Slf4j
@GRpcService
public class FeedInfoService extends FeedServiceGrpc.FeedServiceImplBase {

    private static final int DEFAULT_LAST_COMMENT_COUNT = 5;
    private static final String DEFAULT_FEED_TEXT = "于%d年%d月%d日%d:%d，创建了属于我的岛";

    private final ManagedChannel managedChannel;
    private final MongoTemplate mongoTemplate;
    private final CommentInfoRepository commentInfoRepository;
    private final FeedInfoRepository feedInfoRepository;
    private final LongIdGenerator idGenerator;

    @Autowired
    public FeedInfoService(MongoTemplate mongoTemplate, CommentInfoRepository commentInfoRepository, @Qualifier("couaChannel")ManagedChannel managedChannel, FeedInfoRepository feedInfoRepository, LongIdGenerator idGenerator) {
        this.mongoTemplate = mongoTemplate;
        this.commentInfoRepository = commentInfoRepository;
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
        ProtocolStringList islandIdList = request.getIslandIdList();
        ProtocolStringList hostIdList = request.getHostIdList();
        String text = request.hasText() ? request.getText().getValue() : "";
        List<FeedInfo> feedInfoList = new ArrayList<>();
        IntStream.range(0, islandIdList.size()).forEach(i -> {
            FeedInfo feedInfo = new FeedInfo();
            feedInfo.setId(String.valueOf(idGenerator.nextId()));
            feedInfo.setIslandId(islandIdList.get(i));
            feedInfo.setUserId(userId);
            feedInfo.setHostId(hostIdList.get(i));
            feedInfo.setImageUrls(request.getImageUrisList());
            feedInfo.setText(text);
            feedInfo.setRepostCount(0);
            feedInfo.setCommentsCount(0);
            feedInfo.setLikesCount(0);
            feedInfo.setDeleted(false);
            feedInfo.setCreatedTime(System.currentTimeMillis());
            feedInfoList.add(feedInfo);
        });
        feedInfoRepository.saveAll(feedInfoList);
        //调用coua服务更新island的lastFeedAt字段
        callCouaUpdateIslandLastFeedAt(islandIdList);
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
        FeedInfo feedInfo = feedInfoRepository.findFeedInfoByIdAndDeletedIsFalse(feedId);
        if (feedInfo != null) {
            FeedMessage feedMessage = getFeedMessage(feedInfo);
            responseBuilder.setFeed(feedMessage)
                    .setUserId(feedInfo.getUserId())
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
                    .where("hostId").is(request.getUserId())
                    .and("islandId").is(condition.getIslandId().getValue());
            query.addCriteria(criteria);
        } else if (fromHost || hasIslandId) { //只有一个条件
            Criteria criteria = fromHost ? Criteria.where("hostId").is(request.getUserId())
                    : Criteria.where("islandId").is(condition.getIslandId().getValue());
            query.addCriteria(criteria);
        }

        // 没有条件
        query.with(Sort.by(Sort.Order.desc("createdTime")));
        long totalCount = mongoTemplate.count(query, FeedInfo.class);
        List<FeedInfo> feedInfoList = mongoTemplate.find(query.with(PageRequest.of(page, pageSize)), FeedInfo.class);
        List<FeedMessage> feedMessageList = feedInfoList.stream().map(this::getFeedMessage).filter(Objects::nonNull).collect(Collectors.toList());
        PageResponse pageResponse = PageRequestResponseUtils.buildPageResponse(page, pageSize, totalCount);
        FeedsResponse feedsResponse = FeedsResponse.newBuilder()
                .addAllFeed(feedMessageList)
                .setPageResponse(pageResponse)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(feedsResponse);
        responseObserver.onCompleted();
    }

    /**
     * 根据userId返回该用户最新一条Feed的信息
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveLatestFeedByUserId(RetrieveLatestFeedByUserIdRequest request, StreamObserver<FeedResponse> responseObserver) {
        String userId = request.getUserId();
        FeedInfo feedInfo = feedInfoRepository.findTopByUserIdAndDeletedIsFalseOrderByCreatedTimeDesc(userId);
        if (feedInfo == null) {
            responseObserver.onNext(FeedResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEED_NOT_FOUND_ERROR))
                    .build());
            responseObserver.onCompleted();
            return;
        }
        FeedResponse feedResponse = FeedResponse.newBuilder()
                .setUserId(userId)
                .setFeed(getFeedMessage(feedInfo))
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(feedResponse);
        responseObserver.onCompleted();
    }

    /**
     * 用户创建岛成功后，默认向岛内发送一条Feed
     * @param request
     * @param responseObserver
     */
    @Override
    public void createDefaultFeed(CreateDefaultFeedRequest request, StreamObserver<CreateDefaultFeedResponse> responseObserver) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String text = String.format(DEFAULT_FEED_TEXT, localDateTime.getYear(),
                                                       localDateTime.getMonth().getValue(),
                                                       localDateTime.getDayOfMonth(),
                                                       localDateTime.getHour(),
                                                       localDateTime.getMinute());
        FeedInfo feedInfo = new FeedInfo();
        feedInfo.setId(String.valueOf(idGenerator.nextId()));
        feedInfo.setIslandId(request.getIslandId());
        feedInfo.setUserId(request.getUserId());
        feedInfo.setHostId(request.getHostId());
        feedInfo.setText(text);
        feedInfo.setRepostCount(0);
        feedInfo.setCommentsCount(0);
        feedInfo.setLikesCount(0);
        feedInfo.setDeleted(false);
        feedInfo.setCreatedTime(System.currentTimeMillis());
        feedInfoRepository.save(feedInfo);

        CreateDefaultFeedResponse response = CreateDefaultFeedResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public FeedMessage getFeedMessageById(String feedId) {
        Optional<FeedInfo> feedInfoOptional = feedInfoRepository.findById(feedId);
        if (feedInfoOptional.isPresent()) {
            return getFeedMessage(feedInfoOptional.get());
        } else {
            return FeedMessage.newBuilder().build();
        }
    }

    public void incFeedCount(String feedId, String type) {
        Update update = new Update();
        update.inc(type, 1);
        mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(feedId)), update, FeedInfo.class);
    }

    public void subFeedCount(String feedId, String type) {
        Update update = new Update();
        update.inc(type, -1);
        mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(feedId)), update, FeedInfo.class);
    }

    private FeedMessage getFeedMessage(FeedInfo feedInfo) {
        if (feedInfo == null)
            return null;
        List<CommentMessage> lastCommentMessage = getLastCommentMessage(feedInfo.getId(), DEFAULT_LAST_COMMENT_COUNT);
        return FeedMessage.newBuilder()
                .setId(feedInfo.getId())
                .setIslandId(feedInfo.getIslandId())
                .setUserId(feedInfo.getUserId())
                .setText(feedInfo.getText())
                .addAllImageUris(feedInfo.getImageUrls())
                .setCreatedAt(feedInfo.getCreatedTime())
                .setCommentsCount(feedInfo.getCommentsCount())
                .setLikesCount(feedInfo.getLikesCount())
                .setRepostCount(feedInfo.getRepostCount())
                .addAllLastComments(lastCommentMessage)
                .build();
    }

    private List<CommentMessage> getLastCommentMessage(String feedId, int commentCount) {
        Pageable pageable = PageRequest.of(0, commentCount);
        List<CommentInfo> commentInfoList = commentInfoRepository.getCommentInfosByFeedIdAndDeletedIsFalseOrderByCreatedTimeDesc(feedId, pageable).getContent();

        return commentInfoList.stream().map(CommentService::getCommentMessage).collect(Collectors.toList());
    }

    private void callCouaUpdateIslandLastFeedAt(List<String> islandIdList) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(this.managedChannel);
        UpdateLastFeedAtRequest request = UpdateLastFeedAtRequest.newBuilder()
                .addAllIslandIds(islandIdList)
                .setTimestamps(System.currentTimeMillis())
                .build();
        try {
            stub.updateLastFeedAtById(request);
        } catch (Exception e) {
            log.error("callCouaUpdateIslandLastFeedAt failure! exception: {}", e.getMessage());
        }
    }
}
