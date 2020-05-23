package com.keepreal.madagascar.fossa.grpcController;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
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
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.service.IslandService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the feed GRpc controller.
 */
@GRpcService
public class FeedGRpcController extends FeedServiceGrpc.FeedServiceImplBase {

    private static final String DEFAULT_FEED_TEXT = "于yyyy年MM月dd日HH:mm，创建了属于我的岛。";
    private final LongIdGenerator idGenerator;
    private final IslandService islandService;
    private final FeedInfoService feedInfoService;
    private final MongoTemplate mongoTemplate;

    /**
     * Constructs the feed grpc controller
     *
     * @param idGenerator       {@link LongIdGenerator}
     * @param islandService     {@link IslandService}
     * @param feedInfoService   {@link FeedInfoService}
     * @param mongoTemplate     {@link MongoTemplate}
     */
    public FeedGRpcController(LongIdGenerator idGenerator,
                              IslandService islandService,
                              FeedInfoService feedInfoService,
                              MongoTemplate mongoTemplate) {
        this.idGenerator = idGenerator;
        this.islandService = islandService;
        this.feedInfoService = feedInfoService;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * implements the create feeds method
     *
     * @param request           {@link NewFeedsRequest}.
     * @param responseObserver  {@link NewFeedsResponse} Callback.
     */
    @Override
    public void createFeeds(NewFeedsRequest request, StreamObserver<NewFeedsResponse> responseObserver) {
        String userId = request.getUserId();
        ProtocolStringList islandIdList = request.getIslandIdList();
        ProtocolStringList hostIdList = request.getHostIdList();
        String text = request.hasText() ? request.getText().getValue() : "";

        List<FeedInfo> feedInfoList = new ArrayList<>();
        IntStream.range(0, islandIdList.size()).forEach(i -> {
            FeedInfo.FeedInfoBuilder builder = FeedInfo.builder();
            builder.id(String.valueOf(idGenerator.nextId()));
            builder.islandId(islandIdList.get(i));
            builder.userId(userId);
            builder.hostId(hostIdList.get(i));
            builder.fromHost(userId.equals(hostIdList.get(i)));
            builder.imageUrls(request.getImageUrisList());
            builder.text(text);
            builder.createdTime(System.currentTimeMillis());
            feedInfoList.add(builder.build());
        });

        feedInfoService.saveAll(feedInfoList);
        islandService.callCouaUpdateIslandLastFeedAt(islandIdList);

        NewFeedsResponse newFeedsResponse = NewFeedsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(newFeedsResponse);
        responseObserver.onCompleted();
    }

    /**
     * implements the delete feed by id method
     *
     * @param request           {@link DeleteFeedByIdRequest}.
     * @param responseObserver  {@link DeleteFeedResponse} Callback.
     */
    @Override
    public void deleteFeedById(DeleteFeedByIdRequest request, StreamObserver<DeleteFeedResponse> responseObserver) {
        String feedId = request.getId();

        feedInfoService.deleteFeedById(feedId);

        DeleteFeedResponse deleteFeedResponse = DeleteFeedResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(deleteFeedResponse);
        responseObserver.onCompleted();
    }

    /**
     * implements the get feed by id method
     *
     * @param request           {@link RetrieveFeedByIdRequest}.
     * @param responseObserver  {@link FeedResponse} Callback.
     */
    @Override
    public void retrieveFeedById(RetrieveFeedByIdRequest request, StreamObserver<FeedResponse> responseObserver) {
        FeedResponse.Builder responseBuilder = FeedResponse.newBuilder();
        String userId = request.getUserId();
        String feedId = request.getId();

        FeedInfo feedInfo = feedInfoService.findFeedInfoById(feedId, request.getIncludeDeleted());

        if (feedInfo != null) {
            FeedMessage feedMessage = feedInfoService.getFeedMessage(feedInfo, userId);
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
     * implements the get feeds by condition method
     *
     * @param request           {@link RetrieveMultipleFeedsRequest}.
     * @param responseObserver  {@link FeedsResponse} Callback.
     */
    @Override
    public void retrieveMultipleFeeds(RetrieveMultipleFeedsRequest request, StreamObserver<FeedsResponse> responseObserver) {
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();
        QueryFeedCondition condition = request.getCondition();
        boolean fromHost = condition.hasFromHost();
        boolean hasIslandId = condition.hasIslandId();
        String userId = request.getUserId();

        Query query = new Query();
        query.addCriteria(Criteria.where("deleted").is(false));
        if (fromHost && hasIslandId) {
            Criteria criteria = Criteria
                    .where("fromHost").is(true)
                    .and("islandId").is(condition.getIslandId().getValue());
            query.addCriteria(criteria);
        } else if (fromHost || hasIslandId) {
            Criteria criteria = fromHost ? Criteria.where("fromHost").is(true)
                    : Criteria.where("islandId").is(condition.getIslandId().getValue());
            query.addCriteria(criteria);
        }

        // 没有条件
        query.with(Sort.by(Sort.Order.desc("createdTime")));
        long totalCount = mongoTemplate.count(query, FeedInfo.class);
        List<FeedInfo> feedInfoList = mongoTemplate.find(query.with(PageRequest.of(page, pageSize)), FeedInfo.class);
        List<FeedMessage> feedMessageList = feedInfoList.stream().map(info -> feedInfoService.getFeedMessage(info, userId)).filter(Objects::nonNull).collect(Collectors.toList());

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
     * implements the get latest feed by userId method
     *
     * @param request           {@link RetrieveLatestFeedByUserIdRequest}.
     * @param responseObserver  {@link FeedResponse} Callback.
     */
    @Override
    public void retrieveLatestFeedByUserId(RetrieveLatestFeedByUserIdRequest request, StreamObserver<FeedResponse> responseObserver) {
        String userId = request.getUserId();

        FeedInfo feedInfo = feedInfoService.findTopByUserIdAndDeletedIsFalseOrderByCreatedTimeDesc(userId);
        if (feedInfo == null) {
            responseObserver.onNext(FeedResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEED_NOT_FOUND_ERROR))
                    .build());
            responseObserver.onCompleted();
            return;
        }

        FeedResponse feedResponse = FeedResponse.newBuilder()
                .setUserId(userId)
                .setFeed(feedInfoService.getFeedMessage(feedInfo, userId))
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(feedResponse);
        responseObserver.onCompleted();
    }

    /**
     * implements the create default feed method
     *
     * @param request           {@link CreateDefaultFeedRequest}.
     * @param responseObserver  {@link CreateDefaultFeedResponse} Callback.
     */
    @Override
    public void createDefaultFeed(CreateDefaultFeedRequest request, StreamObserver<CreateDefaultFeedResponse> responseObserver) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_FEED_TEXT);
        String text = LocalDateTime.now().format(formatter);

        FeedInfo.FeedInfoBuilder builder = FeedInfo.builder();
        builder.id(String.valueOf(idGenerator.nextId()));
        builder.islandId(request.getIslandId());
        builder.userId(request.getUserId());
        builder.hostId(request.getHostId());
        builder.text(text);
        builder.createdTime(System.currentTimeMillis());
        feedInfoService.insert(builder.build());

        CreateDefaultFeedResponse response = CreateDefaultFeedResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
