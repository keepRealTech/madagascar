package com.keepreal.madagascar.fossa.grpcController;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.MediaType;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.CreateDefaultFeedRequest;
import com.keepreal.madagascar.fossa.CreateDefaultFeedResponse;
import com.keepreal.madagascar.fossa.DeleteFeedByIdRequest;
import com.keepreal.madagascar.fossa.DeleteFeedResponse;
import com.keepreal.madagascar.fossa.FeedGroupFeedResponse;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.FeedsResponse;
import com.keepreal.madagascar.fossa.NewFeedsRequest;
import com.keepreal.madagascar.fossa.NewFeedsRequestV2;
import com.keepreal.madagascar.fossa.NewFeedsResponse;
import com.keepreal.madagascar.fossa.NewWechatFeedsResponse;
import com.keepreal.madagascar.fossa.QueryFeedCondition;
import com.keepreal.madagascar.fossa.RetrieveFeedByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedCountRequest;
import com.keepreal.madagascar.fossa.RetrieveFeedCountResponse;
import com.keepreal.madagascar.fossa.RetrieveFeedsByIdsRequest;
import com.keepreal.madagascar.fossa.RetrieveMultipleFeedsRequest;
import com.keepreal.madagascar.fossa.RetrieveToppedFeedByIdRequest;
import com.keepreal.madagascar.fossa.TimelineFeedsResponse;
import com.keepreal.madagascar.fossa.TopFeedByIdRequest;
import com.keepreal.madagascar.fossa.TopFeedByIdResponse;
import com.keepreal.madagascar.fossa.UpdateFeedPaidByIdRequest;
import com.keepreal.madagascar.fossa.UpdateFeedPaidByIdResponse;
import com.keepreal.madagascar.fossa.UpdateFeedSaveAuthorityRequest;
import com.keepreal.madagascar.fossa.UpdateFeedSaveAuthorityResponse;
import com.keepreal.madagascar.fossa.model.FeedGroup;
import com.keepreal.madagascar.fossa.model.FeedInfo;
import com.keepreal.madagascar.fossa.model.MediaInfo;
import com.keepreal.madagascar.fossa.model.ReactionInfo;
import com.keepreal.madagascar.fossa.service.FeedEventProducerService;
import com.keepreal.madagascar.fossa.service.FeedGroupService;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.service.IslandService;
import com.keepreal.madagascar.fossa.service.PaymentService;
import com.keepreal.madagascar.fossa.service.ReactionService;
import com.keepreal.madagascar.fossa.service.SubscribeMembershipService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.MediaMessageConvertUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private final FeedGroupService feedGroupService;
    private final MongoTemplate mongoTemplate;
    private final FeedEventProducerService feedEventProducerService;
    private final PaymentService paymentService;
    private final SubscribeMembershipService subscribeMembershipService;
    private final ReactionService reactionService;

    /**
     * Constructs the feed grpc controller
     *
     * @param idGenerator                {@link LongIdGenerator}
     * @param islandService              {@link IslandService}
     * @param feedInfoService            {@link FeedInfoService}
     * @param feedGroupService           {@link FeedGroupService}.
     * @param mongoTemplate              {@link MongoTemplate}
     * @param feedEventProducerService   {@link FeedEventProducerService}.
     * @param paymentService             {@link PaymentService}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     * @param reactionService            {@link ReactionService}.
     */
    public FeedGRpcController(LongIdGenerator idGenerator,
                              IslandService islandService,
                              FeedInfoService feedInfoService,
                              FeedGroupService feedGroupService,
                              MongoTemplate mongoTemplate,
                              FeedEventProducerService feedEventProducerService,
                              PaymentService paymentService,
                              SubscribeMembershipService subscribeMembershipService,
                              ReactionService reactionService) {
        this.idGenerator = idGenerator;
        this.islandService = islandService;
        this.feedInfoService = feedInfoService;
        this.feedGroupService = feedGroupService;
        this.mongoTemplate = mongoTemplate;
        this.feedEventProducerService = feedEventProducerService;
        this.paymentService = paymentService;
        this.subscribeMembershipService = subscribeMembershipService;
        this.reactionService = reactionService;
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
     * Creates feeds with multimedia.
     *
     * @param request          {@link NewFeedsRequestV2}.
     * @param responseObserver {@link NewFeedsResponse}.
     */
    @Override
    public void createFeedsV2(NewFeedsRequestV2 request, StreamObserver<NewFeedsResponse> responseObserver) {
        String userId = request.getUserId();
        ProtocolStringList islandIdList = request.getIslandIdList();
        ProtocolStringList hostIdList = request.getHostIdList();
        String text = request.hasText() ? request.getText().getValue() : "";
        ProtocolStringList membershipIdsList = request.getMembershipIdsList();
        MediaType mediaType = request.getType();

        String duplicateTag = UUID.randomUUID().toString();

        List<FeedInfo> feedInfoList = new ArrayList<>();
        long timestamp = Instant.now().toEpochMilli();
        IntStream.range(0, islandIdList.size()).forEach(i -> {
            FeedInfo.FeedInfoBuilder builder = FeedInfo.builder();
            builder.id(String.valueOf(this.idGenerator.nextId()));
            builder.islandId(islandIdList.get(i));
            builder.userId(userId);
            builder.hostId(hostIdList.get(i));
            builder.fromHost(userId.equals(hostIdList.get(i)));
            builder.text(text);
            builder.duplicateTag(duplicateTag);
            builder.multiMediaType(mediaType.name());
            builder.mediaInfos(this.buildMediaInfos(request));
            builder.membershipIds(membershipIdsList);
            builder.createdTime(timestamp);
            builder.toppedTime(timestamp);
            if (request.hasPriceInCents()) {
                builder.priceInCents(request.getPriceInCents().getValue());
            }
            builder.userMembershipIds(this.subscribeMembershipService.retrieveMembershipIds(userId, islandIdList.get(i)));
            feedInfoList.add(builder.build());
        });

        if (request.hasFeedGroupId() && 1 == islandIdList.size()) {
            FeedGroup feedGroup = this.feedGroupService.retrieveFeedGroupById(request.getFeedGroupId().getValue());
            if (Objects.isNull(feedGroup)) {
                NewFeedsResponse response = NewFeedsResponse.newBuilder()
                        .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEEDGROUP_NOT_FOUND_ERROR))
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

            feedInfoList.get(0).setFeedGroupId(request.getFeedGroupId().getValue());

            if ((MediaType.MEDIA_PICS == mediaType || MediaType.MEDIA_ALBUM == mediaType) && membershipIdsList.isEmpty()) {
                feedGroup.getImageFeedIds().add(feedInfoList.get(0).getId());
            }
            feedGroup.getFeedIds().add(feedInfoList.get(0).getId());
            feedGroup.setLastFeedTime(feedInfoList.get(0).getCreatedTime());
            this.feedGroupService.updateFeedGroup(feedGroup);
        }

        List<FeedInfo> feedInfos = this.feedInfoService.saveAll(feedInfoList);
        this.islandService.callCouaUpdateIslandLastFeedAt(islandIdList, timestamp);

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
        FeedInfo feedInfo = this.feedInfoService.findFeedInfoById(request.getId(), false);

        if (Objects.isNull(feedInfo)) {
            DeleteFeedResponse deleteFeedResponse = DeleteFeedResponse.newBuilder()
                    .setStatus(CommonStatusUtils.getSuccStatus())
                    .build();
            responseObserver.onNext(deleteFeedResponse);
            responseObserver.onCompleted();
        }

        if (!StringUtils.isEmpty(feedInfo.getFeedGroupId())) {
            FeedGroup feedGroup = this.feedGroupService.retrieveFeedGroupById(feedInfo.getFeedGroupId());
            if (Objects.nonNull(feedGroup)) {
                feedGroup.getFeedIds().remove(feedInfo.getId());
                feedGroup.getImageFeedIds().remove(feedInfo.getId());
                this.feedGroupService.updateFeedGroup(feedGroup);
            }
        }

        this.feedInfoService.deleteFeedById(request.getId());
        this.feedEventProducerService.produceDeleteFeedEventAsync(request.getId());

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

        FeedInfo feedInfo = this.feedInfoService.findFeedInfoById(feedId, request.getIncludeDeleted());

        if (feedInfo != null) {
            FeedMessage feedMessage = this.feedInfoService.getFeedMessage(feedInfo, userId);
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
     * Implements the feed by id with feed group infos.
     *
     * @param request          {@link RetrieveFeedByIdRequest}.
     * @param responseObserver {@link FeedGroupFeedResponse}.
     */
    @Override
    public void retrieveFeedGroupFeedById(RetrieveFeedByIdRequest request,
                                          StreamObserver<FeedGroupFeedResponse> responseObserver) {
        FeedGroupFeedResponse.Builder responseBuilder = FeedGroupFeedResponse.newBuilder();
        FeedInfo feedInfo = this.feedInfoService.findFeedInfoById(request.getId(), request.getIncludeDeleted());

        if (Objects.isNull(feedInfo)) {
            responseBuilder
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEED_NOT_FOUND_ERROR));
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }

        responseBuilder
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setFeed(this.feedInfoService.getFeedMessage(feedInfo, request.getUserId()));

        FeedGroup feedGroup = null;
        if (!StringUtils.isEmpty(feedInfo.getFeedGroupId())) {
            feedGroup = this.feedGroupService.retrieveFeedGroupById(feedInfo.getFeedGroupId());
        }
        if (Objects.isNull(feedGroup)) {
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }

        String lastFeedId = feedGroup.getFeedIds().lower(request.getId());
        String nextFeedId = feedGroup.getFeedIds().higher(request.getId());
        responseBuilder
                .setFeedGroup(this.feedGroupService.getFeedGroupMessage(feedGroup))
                .setLastFeedId(Objects.isNull(lastFeedId) ? "" : lastFeedId)
                .setNextFeedId(Objects.isNull(nextFeedId) ? "" : nextFeedId);

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
        Query query = buildQueryByRequest(request);
        long totalCount = mongoTemplate.count(query, FeedInfo.class);
        List<FeedInfo> feedInfoList = mongoTemplate.find(query.with(PageRequest.of(page, pageSize)), FeedInfo.class);

        List<String> myMembershipIds = this.subscribeMembershipService.retrieveMembershipIds(request.getUserId(), null);
        List<FeedMessage> feedMessageList = feedInfoList.stream()
                .map(info -> feedInfoService.getFeedMessage(info, userId, myMembershipIds))
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

        List<String> myMembershipIds = this.subscribeMembershipService.retrieveMembershipIds(request.getUserId(), null);

        Set<String> likedFeedIds = this.reactionService.retrieveReactionsByFeedIdsAndUserId(feedInfoList.stream().map(FeedInfo::getId).collect(Collectors.toSet()), request.getUserId()).stream()
                .map(ReactionInfo::getFeedId).collect(Collectors.toSet());

        List<FeedMessage> feedMessageList = feedInfoList.stream()
                .map(info -> this.feedInfoService.getFeedMessage(info, request.getUserId(), myMembershipIds, likedFeedIds.contains(info.getId())))
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
     * @param request          {@link RetrieveMultipleFeedsRequest}.
     * @param responseObserver {@link TimelineFeedsResponse}.
     */
    @Override
    public void retrieveMultipleTimelineFeeds(RetrieveMultipleFeedsRequest request, StreamObserver<TimelineFeedsResponse> responseObserver) {
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();
        Query query = buildQueryByRequest(request);
        List<FeedInfo> feedInfoList = mongoTemplate.find(query.with(PageRequest.of(page, pageSize)), FeedInfo.class);

        responseObserver.onNext(TimelineFeedsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllMessage(feedInfoList.stream()
                        .map(this.feedInfoService::getTimelineFeedMessage)
                        .collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * Builds the query.
     *
     * @param request {@link RetrieveMultipleFeedsRequest}.
     * @return {@link Query}.
     */
    private Query buildQueryByRequest(RetrieveMultipleFeedsRequest request) {
        QueryFeedCondition condition = request.getCondition();
        boolean fromHost = condition.hasFromHost();
        boolean hasIslandId = condition.hasIslandId();

        Query query = new Query();
        query.addCriteria(Criteria.where("deleted").is(false));
        query.addCriteria(Criteria.where("multiMediaType").ne(MediaType.MEDIA_QUESTION.name()));
        query.addCriteria(Criteria.where("temped").ne(true));
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

        if (condition.hasExcludeTopped()) {
            boolean value = condition.getExcludeTopped().getValue();
            Criteria criteria = new Criteria();
            if (value) {
                criteria = Criteria.where("isTop").is(false);
            }
            query.addCriteria(criteria);
        }

        // 没有条件
        return query.with(Sort.by(Sort.Order.desc("createdTime"), Sort.Order.desc("toppedTime")));
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
        } else {
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
        FeedResponse.Builder builder = FeedResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setUserId(userId);
        if (Objects.nonNull(feedMessage)) {
            builder.setFeed(feedMessage);
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createWechatFeedsV2(NewFeedsRequestV2 request, StreamObserver<NewWechatFeedsResponse> responseObserver) {
        String userId = request.getUserId();
        String islandId = request.getIslandIdList().get(0);
        String hostId = request.getHostIdList().get(0);
        String text = request.hasText() ? request.getText().getValue() : "";
        ProtocolStringList membershipIdsList = request.getMembershipIdsList();
        MediaType mediaType = request.getType();

        String duplicateTag = UUID.randomUUID().toString();

        long timestamp = Instant.now().toEpochMilli();
        FeedInfo.FeedInfoBuilder builder = FeedInfo.builder();
        builder.id(String.valueOf(idGenerator.nextId()));
        builder.islandId(islandId);
        builder.userId(userId);
        builder.hostId(hostId);
        builder.fromHost(userId.equals(hostId));
        builder.text(text);
        builder.duplicateTag(duplicateTag);
        builder.multiMediaType(mediaType.name());
        builder.mediaInfos(this.buildMediaInfos(request));
        builder.membershipIds(membershipIdsList);
        builder.createdTime(timestamp);
        builder.toppedTime(timestamp);
        builder.temped(true);
        if (request.hasPriceInCents()) {
            builder.priceInCents(request.getPriceInCents().getValue());
        }
        builder.userMembershipIds(this.subscribeMembershipService.retrieveMembershipIds(userId, islandId));

        FeedInfo feedInfo = feedInfoService.insert(builder.build());

        NewWechatFeedsResponse newFeedsResponse = NewWechatFeedsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setMessage(this.paymentService.wechatCreateFeed(feedInfo.getId(), request.getPriceInCents().getValue(), userId, hostId))
                .build();
        responseObserver.onNext(newFeedsResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateFeedPaidById(UpdateFeedPaidByIdRequest request, StreamObserver<UpdateFeedPaidByIdResponse> responseObserver) {
        String feedId = request.getId();
        FeedInfo feedInfo = this.feedInfoService.findFeedInfoById(feedId, false);
        feedInfo.setTemped(false);
        this.feedInfoService.update(feedInfo);

        responseObserver.onNext(UpdateFeedPaidByIdResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveFeedCountByIslandId(RetrieveFeedCountRequest request, StreamObserver<RetrieveFeedCountResponse> responseObserver) {
        Query query = new Query();
        query.addCriteria(Criteria.where("islandId").is(request.getIslandId()));
        query.addCriteria(Criteria.where("deleted").is(false));
        query.addCriteria(Criteria.where("multiMediaType").ne(MediaType.MEDIA_QUESTION.name()));

        long totalCount = mongoTemplate.count(query, FeedInfo.class);

        responseObserver.onNext(RetrieveFeedCountResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setFeedCount(Math.toIntExact(totalCount))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateFeedSaveAuthority(UpdateFeedSaveAuthorityRequest request, StreamObserver<UpdateFeedSaveAuthorityResponse> responseObserver) {
        String feedId = request.getFeedId();
        boolean canSave = request.getCanSave();

        FeedInfo feedInfo = this.feedInfoService.findFeedInfoById(feedId, false);

        if (feedInfo == null) {
            responseObserver.onNext(UpdateFeedSaveAuthorityResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FEED_NOT_FOUND_ERROR))
                    .build());
            responseObserver.onCompleted();
            return;
        }

        feedInfo.setCanSave(canSave);
        this.feedInfoService.update(feedInfo);

        responseObserver.onNext(UpdateFeedSaveAuthorityResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build());
        responseObserver.onCompleted();
    }

    private List<MediaInfo> buildMediaInfos(NewFeedsRequestV2 request) {
        List<MediaInfo> mediaInfos = new ArrayList<>();
        switch (request.getType()) {
            case MEDIA_PICS:
            case MEDIA_ALBUM:
                mediaInfos.addAll(MediaMessageConvertUtils.toPictureInfoList(request.getPics()));
                break;
            case MEDIA_VIDEO:
                mediaInfos.add(MediaMessageConvertUtils.toVideoInfo(request.getVideo()));
                break;
            case MEDIA_AUDIO:
                mediaInfos.add(MediaMessageConvertUtils.toAudioInfo(request.getAudio()));
                break;
            case MEDIA_HTML:
                mediaInfos.add(MediaMessageConvertUtils.toHtmlInfo(request.getHtml()));
                break;
            case MEDIA_QUESTION:
                break;
        }
        return mediaInfos;
    }
}
