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
import com.keepreal.madagascar.fossa.RetrieveFeedsByIdsRequest;
import com.keepreal.madagascar.fossa.RetrieveMultipleFeedsRequest;
import com.keepreal.madagascar.fossa.RetrieveToppedFeedByIdRequest;
import com.keepreal.madagascar.fossa.TimelineFeedsResponse;
import com.keepreal.madagascar.fossa.TopFeedByIdRequest;
import com.keepreal.madagascar.fossa.TopFeedByIdResponse;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.service.FeedEventProducerService;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.service.IslandService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the feed GRpc controller.
 */
@Slf4j
@GRpcService
public class FeedGRpcController extends FeedServiceGrpc.FeedServiceImplBase {

    private static final String DEFAULT_FEED_TEXT = "于yyyy年MM月dd日HH:mm，创建了属于我的岛。";
    private final LongIdGenerator idGenerator;
    private final IslandService islandService;
    private final FeedInfoService feedInfoService;
    private final MongoTemplate mongoTemplate;
    private final FeedEventProducerService feedEventProducerService;

    /**
     * Constructs the feed grpc controller
     *
     * @param idGenerator              {@link LongIdGenerator}
     * @param islandService            {@link IslandService}
     * @param feedInfoService          {@link FeedInfoService}
     * @param mongoTemplate            {@link MongoTemplate}
     * @param feedEventProducerService {@link FeedEventProducerService}.
     */
    public FeedGRpcController(LongIdGenerator idGenerator,
                              IslandService islandService,
                              FeedInfoService feedInfoService,
                              MongoTemplate mongoTemplate,
                              FeedEventProducerService feedEventProducerService) {
        this.idGenerator = idGenerator;
        this.islandService = islandService;
        this.feedInfoService = feedInfoService;
        this.mongoTemplate = mongoTemplate;
        this.feedEventProducerService = feedEventProducerService;
    }

    /**
     * implements the create feeds method
     *
     * @param request          {@link NewFeedsRequest}.
     * @param responseObserver {@link NewFeedsResponse} Callback.
     */
    @Override
    public void createFeeds(NewFeedsRequest request, StreamObserver<NewFeedsResponse> responseObserver) {
        String userId = request.getUserId();
        ProtocolStringList islandIdList = request.getIslandIdList();
        ProtocolStringList hostIdList = request.getHostIdList();
        String text = request.hasText() ? request.getText().getValue() : "";
        ProtocolStringList membershipIdsList = request.getMembershipIdsList();

        String duplicateTag = UUID.randomUUID().toString();

        List<FeedInfo> feedInfoList = new ArrayList<>();
        long timestamp = Instant.now().toEpochMilli();
        IntStream.range(0, islandIdList.size()).forEach(i -> {
            FeedInfo.FeedInfoBuilder builder = FeedInfo.builder();
            builder.id(String.valueOf(idGenerator.nextId()));
            builder.islandId(islandIdList.get(i));
            builder.userId(userId);
            builder.hostId(hostIdList.get(i));
            builder.fromHost(userId.equals(hostIdList.get(i)));
            builder.imageUrls(request.getImageUrisList());
            builder.text(text);
            builder.duplicateTag(duplicateTag);
            builder.membershipIds(membershipIdsList);
            builder.createdTime(timestamp);
            builder.toppedTime(timestamp);
            feedInfoList.add(builder.build());
        });

        List<FeedInfo> feedInfos = feedInfoService.saveAll(feedInfoList);
        islandService.callCouaUpdateIslandLastFeedAt(islandIdList, timestamp);

        feedInfos.forEach(this.feedEventProducerService::produceNewFeedEventAsync);

        NewFeedsResponse newFeedsResponse = NewFeedsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(newFeedsResponse);
        responseObserver.onCompleted();
    }

    /**
     * implements the delete feed by id method
     *
     * @param request          {@link DeleteFeedByIdRequest}.
     * @param responseObserver {@link DeleteFeedResponse} Callback.
     */
    @Override
    public void deleteFeedById(DeleteFeedByIdRequest request, StreamObserver<DeleteFeedResponse> responseObserver) {
        String feedId = request.getId();

        feedInfoService.deleteFeedById(feedId);

        this.feedEventProducerService.produceDeleteFeedEventAsync(feedId);

        DeleteFeedResponse deleteFeedResponse = DeleteFeedResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(deleteFeedResponse);
        responseObserver.onCompleted();
    }

    /**
     * implements the get feed by id method
     *
     * @param request          {@link RetrieveFeedByIdRequest}.
     * @param responseObserver {@link FeedResponse} Callback.
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
            log.error("[retrieveFeedById] feed not found error! feed id is [{}]", feedId);
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEED_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * implements the get feeds by condition method
     *
     * @param request          {@link RetrieveMultipleFeedsRequest}.
     * @param responseObserver {@link FeedsResponse} Callback.
     */
    @Override
    public void retrieveMultipleFeeds(RetrieveMultipleFeedsRequest request, StreamObserver<FeedsResponse> responseObserver) {
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();
        String userId = request.getUserId();
        Query query = generatorQueryByRequest(request);
        query.addCriteria(Criteria.where("isTop").is(false));
        long totalCount = mongoTemplate.count(query, FeedInfo.class);
        List<FeedInfo> feedInfoList = mongoTemplate.find(query.with(PageRequest.of(page, pageSize)), FeedInfo.class);
        List<FeedMessage> feedMessageList = feedInfoList.stream()
                .map(info -> feedInfoService.getFeedMessage(info, userId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

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
     * implements the create default feed method
     *
     * @param request          {@link CreateDefaultFeedRequest}.
     * @param responseObserver {@link CreateDefaultFeedResponse} Callback.
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
        builder.fromHost(request.getUserId().equals(request.getHostId()));
        builder.text(text);
        builder.createdTime(System.currentTimeMillis());
        feedInfoService.insert(builder.build());

        CreateDefaultFeedResponse response = CreateDefaultFeedResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the feeds retrieve by ids method.
     *
     * @param request          {@link RetrieveFeedsByIdsRequest}.
     * @param responseObserver {@link RetrieveFeedsByIdsRequest} Callback.
     */
    @Override
    public void retrieveFeedsByIds(RetrieveFeedsByIdsRequest request, StreamObserver<FeedsResponse> responseObserver) {
        List<FeedInfo> feedInfoList = this.feedInfoService.findByIds(request.getIdsList());
        List<FeedMessage> feedMessageList = feedInfoList.stream()
                .map(info -> this.feedInfoService.getFeedMessage(info, request.getUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        FeedsResponse feedsResponse = FeedsResponse.newBuilder()
                .addAllFeed(feedMessageList)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(feedsResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the retrieves multiple timeline feeds method.
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveMultipleTimelineFeeds(RetrieveMultipleFeedsRequest request, StreamObserver<TimelineFeedsResponse> responseObserver) {
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();
        Query query = generatorQueryByRequest(request);
        List<FeedInfo> feedInfoList = mongoTemplate.find(query.with(PageRequest.of(page, pageSize)), FeedInfo.class);

        responseObserver.onNext(TimelineFeedsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllMessage(feedInfoList.stream()
                        .map(this.feedInfoService::getTimelineFeedMessage)
                        .collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }

    private Query generatorQueryByRequest(RetrieveMultipleFeedsRequest request) {
        QueryFeedCondition condition = request.getCondition();
        boolean fromHost = condition.hasFromHost();
        boolean hasIslandId = condition.hasIslandId();

        Query query = new Query();
        query.addCriteria(Criteria.where("deleted").is(false));
        if (fromHost && hasIslandId) {
            Criteria criteria = Criteria
                    .where("islandId").is(condition.getIslandId().getValue())
                    .and("fromHost").is(true);
            query.addCriteria(criteria);
        } else if (fromHost || hasIslandId) {
            Criteria criteria = fromHost ? Criteria.where("fromHost").is(true)
                    : Criteria.where("islandId").is(condition.getIslandId().getValue());
            query.addCriteria(criteria);
        }

        if (condition.hasTimestampBefore() && condition.hasTimestampAfter()) {
            Criteria timeCriteria = Criteria.where("createdTime")
                    .gt(condition.getTimestampAfter().getValue())
                    .andOperator(Criteria.where("createdTime")
                            .lt(condition.getTimestampBefore().getValue()));
            query.addCriteria(timeCriteria);
        } else if (condition.hasTimestampBefore() || condition.hasTimestampAfter()) {
            Criteria timeCriteria = condition.hasTimestampBefore() ?
                    Criteria.where("createdTime").lt(condition.getTimestampBefore().getValue()) :
                    Criteria.where("createdTime").gt(condition.getTimestampAfter().getValue());
            query.addCriteria(timeCriteria);
        }

        // 没有条件
        return query.with(Sort.by(Sort.Order.desc("toppedTime"), Sort.Order.desc("createdTime")));
    }

    /**
     * top feed or cancel topped feed
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void topFeedById(TopFeedByIdRequest request, StreamObserver<TopFeedByIdResponse> responseObserver) {
        String feedId = request.getId();
        String islandId = request.getIslandId();
        boolean isRevoke = request.getIsRevoke();
        if (!isRevoke) {
            //cancel topped feed of this island  this version (v1.2) can only top one feed
            this.feedInfoService.cancelToppedFeedByIslandId(islandId);

            this.feedInfoService.topFeedById(feedId);
        }else {
            this.feedInfoService.cancelToppedFeedById(feedId);
        }

        TopFeedByIdResponse topFeedByIdResponse = TopFeedByIdResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(topFeedByIdResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveToppedFeedById(RetrieveToppedFeedByIdRequest request, StreamObserver<FeedResponse> responseObserver) {
        String islandId = request.getIslandId();
        String userId = request.getUserId();
        FeedInfo feedInfo = this.feedInfoService.findToppedFeedByIslandId(islandId);
        FeedMessage feedMessage = this.feedInfoService.getFeedMessage(feedInfo, userId);
        FeedResponse feedResponse = FeedResponse.newBuilder()
                .setFeed(feedMessage)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setUserId(userId)
                .build();

        responseObserver.onNext(feedResponse);
        responseObserver.onCompleted();
    }
}
