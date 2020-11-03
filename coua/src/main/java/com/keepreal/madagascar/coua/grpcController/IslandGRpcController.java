package com.keepreal.madagascar.coua.grpcController;

import com.aliyuncs.utils.StringUtils;
import com.google.protobuf.Empty;
import com.google.protobuf.ProtocolStringList;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.DeviceType;
import com.keepreal.madagascar.common.IslandAccessType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CheckIslandSubscriptionRequest;
import com.keepreal.madagascar.coua.CheckNameRequest;
import com.keepreal.madagascar.coua.CheckNameResponse;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.coua.CheckNewFeedsRequest;
import com.keepreal.madagascar.coua.CheckNewFeedsResponse;
import com.keepreal.madagascar.coua.CreateOrUpdateSupportTargetRequest;
import com.keepreal.madagascar.coua.DeleteSupportTargetRequest;
import com.keepreal.madagascar.coua.DiscoverIslandMessage;
import com.keepreal.madagascar.coua.DiscoverIslandsRequest;
import com.keepreal.madagascar.coua.DiscoverIslandsResponse;
import com.keepreal.madagascar.coua.DismissIntroductionRequest;
import com.keepreal.madagascar.coua.IslandProfileResponse;
import com.keepreal.madagascar.coua.IslandResponse;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.IslandSubscribersResponse;
import com.keepreal.madagascar.coua.IslandSubscriptionStateResponse;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.NewIslandRequest;
import com.keepreal.madagascar.coua.QueryIslandCondition;
import com.keepreal.madagascar.coua.RetrieveDefaultIslandsByUserIdRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensResponse;
import com.keepreal.madagascar.coua.RetrieveIslandByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandProfileByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandSubscribersByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslanderPortraitUrlRequest;
import com.keepreal.madagascar.coua.RetrieveIslanderPortraitUrlResponse;
import com.keepreal.madagascar.coua.RetrieveMultipleIslandsRequest;
import com.keepreal.madagascar.coua.RetrieveSupportTargetsRequest;
import com.keepreal.madagascar.coua.RetrieveUserSubscriptionStateRequest;
import com.keepreal.madagascar.coua.RetrieveUserSubscriptionStateResponse;
import com.keepreal.madagascar.coua.SubscribeIslandByIdRequest;
import com.keepreal.madagascar.coua.SubscribeIslandResponse;
import com.keepreal.madagascar.coua.SupportTargetResponse;
import com.keepreal.madagascar.coua.SupportTargetsResponse;
import com.keepreal.madagascar.coua.TargetType;
import com.keepreal.madagascar.coua.UnsubscribeIslandByIdRequest;
import com.keepreal.madagascar.coua.UpdateIslandByIdRequest;
import com.keepreal.madagascar.coua.UpdateLastFeedAtRequest;
import com.keepreal.madagascar.coua.UpdateLastFeedAtResponse;
import com.keepreal.madagascar.coua.common.SubscriptionState;
import com.keepreal.madagascar.coua.model.IslandInfo;
import com.keepreal.madagascar.coua.model.Subscription;
import com.keepreal.madagascar.coua.model.SupportTarget;
import com.keepreal.madagascar.coua.model.UserInfo;
import com.keepreal.madagascar.coua.service.FeedService;
import com.keepreal.madagascar.coua.service.IslandEventProducerService;
import com.keepreal.madagascar.coua.service.IslandInfoService;
import com.keepreal.madagascar.coua.service.SubscriptionService;
import com.keepreal.madagascar.coua.service.UserDeviceInfoService;
import com.keepreal.madagascar.coua.service.UserInfoService;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import com.keepreal.madagascar.coua.util.PageResponseUtil;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the island GRpc controller.
 */
@Slf4j
@GRpcService
public class IslandGRpcController extends IslandServiceGrpc.IslandServiceImplBase {

    private final IslandInfoService islandInfoService;
    private final SubscriptionService subscriptionService;
    private final FeedService feedService;
    private final UserInfoService userInfoService;
    private final UserDeviceInfoService userDeviceInfoService;
    private final IslandEventProducerService islandEventProducerService;

    /**
     * Constructs the island grpc controller.
     *
     * @param islandInfoService          {@link IslandInfoService}.
     * @param subscriptionService        {@link SubscriptionService}.
     * @param feedService                {@link FeedService}.
     * @param userInfoService            {@link UserInfoService}.
     * @param userDeviceInfoService      {@link UserDeviceInfoService}.
     * @param islandEventProducerService {@link IslandEventProducerService}
     */
    public IslandGRpcController(IslandInfoService islandInfoService,
                                SubscriptionService subscriptionService,
                                FeedService feedService,
                                UserInfoService userInfoService,
                                UserDeviceInfoService userDeviceInfoService,
                                IslandEventProducerService islandEventProducerService) {
        this.islandInfoService = islandInfoService;
        this.subscriptionService = subscriptionService;
        this.feedService = feedService;
        this.userInfoService = userInfoService;
        this.userDeviceInfoService = userDeviceInfoService;
        this.islandEventProducerService = islandEventProducerService;
    }

    /**
     * Implements the check island name method.
     *
     * @param request          {@link CheckNameRequest}.
     * @param responseObserver {@link CheckNameResponse}.
     */
    @Override
    public void checkName(CheckNameRequest request, StreamObserver<CheckNameResponse> responseObserver) {
        String islandName = request.getName();
        boolean isExisted = islandInfoService.islandNameIsExisted(islandName);
        CheckNameResponse checkNameResponse = CheckNameResponse.newBuilder()
                .setIsExisted(isExisted)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(checkNameResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the create island method.
     *
     * @param request          {@link NewIslandRequest}.
     * @param responseObserver {@link IslandResponse}.
     */
    @Override
    public void createIsland(NewIslandRequest request, StreamObserver<IslandResponse> responseObserver) {
        if (islandInfoService.islandNameIsExisted(request.getName())) {
            log.error("[createIsland] island name existed error! island name is [{}]", request.getName());
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NAME_EXISTED_ERROR);
            responseObserver.onNext(IslandResponse.newBuilder().setStatus(commonStatus).build());
            responseObserver.onCompleted();
            return;
        }

        if (this.islandInfoService.getMyCreatedIslands(request.getHostId(), PageRequest.of(0, 1)).getTotalElements() > 0) {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_CREATE_ERROR);
            responseObserver.onNext(IslandResponse.newBuilder().setStatus(commonStatus).build());
            responseObserver.onCompleted();
            return;
        }

        IslandInfo.IslandInfoBuilder infoBuilder = IslandInfo.builder()
                .hostId(request.getHostId())
                .islandName(request.getName())
                .islandAccessType(request.getIslandAccessTypeValue());
        if (request.hasPortraitImageUri()) {
            infoBuilder.portraitImageUri(request.getPortraitImageUri().getValue());
        }
        if (IslandAccessType.ISLAND_ACCESS_PRIVATE.equals(request.getIslandAccessType()) && request.hasSecret()) {
            infoBuilder.secret(request.getSecret().getValue());
        }
        if (request.hasIdentityId()) {
            infoBuilder.identityId(request.getIdentityId().getValue());
        }
        if (request.hasDescription()) {
            infoBuilder.description(request.getDescription().getValue());
        }
        if (request.hasCustomUrl()) {
            if (this.islandInfoService.checkIslandCustomUrl(request.getCustomUrl().getValue())) {
                IslandResponse islandResponse = IslandResponse.newBuilder()
                        .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_CUSTOM_URL_EXISTED_ERROR))
                        .build();
                responseObserver.onNext(islandResponse);
                responseObserver.onCompleted();
            }
            infoBuilder.customUrl(request.getCustomUrl().getValue());
        }

        IslandInfo save = islandInfoService.createIsland(infoBuilder.build());
        try {
            feedService.createDefaultFeed(request.getHostId(), request.getHostId(), save.getId());
        } catch (KeepRealBusinessException e) {
            log.error("[createIsland] {}! host id is [{}], island id is [{}]", e.getErrorCode().toString(), save.getId());
            responseObserver.onNext(IslandResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(e.getErrorCode()))
                    .build());
            responseObserver.onCompleted();
            return;
        }

        this.islandEventProducerService.produceCreateIslandEventAsync(request.getHostId());

        IslandMessage islandMessage = islandInfoService.getIslandMessage(save);
        IslandResponse islandResponse = IslandResponse.newBuilder()
                .setIsland(islandMessage)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(islandResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the retrieve island by id method.
     *
     * @param request          {@link RetrieveIslandByIdRequest}.
     * @param responseObserver {@link IslandResponse}.
     */
    @Override
    public void retrieveIslandById(RetrieveIslandByIdRequest request, StreamObserver<IslandResponse> responseObserver) {
        IslandResponse.Builder responseBuilder = IslandResponse.newBuilder();
        String islandId = request.getId();
        IslandInfo islandInfo = islandInfoService.findTopByIdAndDeletedIsFalse(islandId);
        if (islandInfo != null) {
            IslandMessage islandMessage = islandInfoService.getIslandMessage(islandInfo);
            responseBuilder.setIsland(islandMessage)
                    .setStatus(CommonStatusUtils.getSuccStatus());
        } else {
            log.error("[retrieveIslandById] island not found error! island id is [{}]", islandId);
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
        }

        IslandResponse islandResponse = responseBuilder.build();
        responseObserver.onNext(islandResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the island profile by id method.
     *
     * @param request          {@link RetrieveIslandProfileByIdRequest}.
     * @param responseObserver {@link IslandProfileResponse}.
     */
    @Override
    public void retrieveIslandProfileById(RetrieveIslandProfileByIdRequest request, StreamObserver<IslandProfileResponse> responseObserver) {
        boolean islandFound = false;
        boolean userFound = false;
        IslandProfileResponse.Builder responseBuilder = IslandProfileResponse.newBuilder();
        IslandInfo islandInfo = islandInfoService.findTopByIdAndDeletedIsFalse(request.getId());
        if (islandInfo != null) {
            islandFound = true;
            UserMessage userMessage = userInfoService.getUserMessageById(islandInfo.getHostId());
            if (userMessage != null) {
                userFound = true;
                IslandMessage islandMessage = islandInfoService.getIslandMessage(islandInfo);
                Subscription subscription = subscriptionService.getSubscriptionByIslandIdAndUserId(islandInfo.getId(), request.getUserId());

                if (subscription == null || subscription.getState() < 0) {
                    responseBuilder.setUserIndex(StringValue.of(""))
                            .setSubscribedAt(0L)
                            .setUserShouldIntroduce(false)
                            .setHostShouldIntroduce(false);
                } else {
                    responseBuilder.setUserIndex(StringValue.of(subscription.getIslanderNumber().toString()))
                            .setSubscribedAt(subscription.getCreatedTime())
                            .setUserShouldIntroduce(subscription.getShouldIntroduce())
                            .setHostShouldIntroduce(subscription.getShouldIntroduce());
                }
                responseBuilder.setIsland(islandMessage)
                        .setHost(userMessage)
                        .setStatus(CommonStatusUtils.getSuccStatus());
            }
        }
        if (!islandFound || !userFound) {
            CommonStatus commonStatus = islandFound ?
                    CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_NOT_FOUND_ERROR) :
                    CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
        }

        IslandProfileResponse islandProfileResponse = responseBuilder
                .build();
        responseObserver.onNext(islandProfileResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the retrieve island by condition method.
     *
     * @param request          {@link RetrieveMultipleIslandsRequest} (hostId, subscriberId, islandName)
     * @param responseObserver {@link IslandsResponse}.
     */
    @Override
    public void retrieveIslandsByCondition(RetrieveMultipleIslandsRequest request, StreamObserver<IslandsResponse> responseObserver) {
        List<IslandInfo> islandInfoList;
        Pageable pageable = PageResponseUtil.getPageable(request.getPageRequest());
        IslandsResponse.Builder builder = IslandsResponse.newBuilder();

        QueryIslandCondition requestCondition = request.getCondition();
        if (requestCondition.hasHostId()) {
            islandInfoList = islandInfoService.getMyCreatedIsland(requestCondition.getHostId().getValue(), pageable, builder);
        } else {
            boolean hasName = requestCondition.hasName();
            boolean hasSubscribedUserId = requestCondition.hasSubscribedUserId();
            if (hasName && hasSubscribedUserId) {
                islandInfoList = islandInfoService.getIslandByNameAndSubscribed(requestCondition.getName().getValue(), requestCondition.getSubscribedUserId().getValue());
            } else if (hasSubscribedUserId) {
                islandInfoList = islandInfoService.getIslandBySubscribed(requestCondition.getSubscribedUserId().getValue(), pageable, builder);
            } else if (hasName) {
                islandInfoList = islandInfoService.getIslandByName(requestCondition.getName().getValue(), pageable, builder);
            } else {
                islandInfoList = islandInfoService.getIsland(pageable, builder);
            }
        }

        List<IslandMessage> islandMessageList = islandInfoList.stream().map(islandInfoService::getIslandMessage).filter(Objects::nonNull).collect(Collectors.toList());
        builder.addAllIslands(islandMessageList);

        IslandsResponse islandsResponse = builder.setStatus(CommonStatusUtils.getSuccStatus()).build();
        responseObserver.onNext(islandsResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the update island by id method.
     *
     * @param request          {@link UpdateIslandByIdRequest}.
     * @param responseObserver {@link IslandResponse}.
     */
    @Override
    public void updateIslandById(UpdateIslandByIdRequest request, StreamObserver<IslandResponse> responseObserver) {
        IslandInfo islandInfo = islandInfoService.findTopByIdAndDeletedIsFalse(request.getId());
        IslandResponse.Builder responseBuilder = IslandResponse.newBuilder();
        if (Objects.isNull(islandInfo)) {
            log.error("[updateIslandById] island not found error! island id is [{}]", request.getId());
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }

        if (request.hasName()) {
            if (islandInfoService.islandNameIsExisted(request.getName().getValue())) {
                CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NAME_EXISTED_ERROR);
                responseBuilder.setStatus(commonStatus);
                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted();
                return;
            }
            islandInfo.setIslandName(request.getName().getValue());
        }
        if (request.hasDescription()) {
            islandInfo.setDescription(request.getDescription().getValue());
        }
        if (request.hasPortraitImageUri()) {
            islandInfo.setPortraitImageUri(request.getPortraitImageUri().getValue());
        }
        if (!IslandAccessType.ISLAND_ACCESS_UNKNOWN.equals(request.getIslandAccessType())) {
            islandInfo.setIslandAccessType(request.getIslandAccessTypeValue());
        }
        if (request.hasSecret() && IslandAccessType.ISLAND_ACCESS_PRIVATE_VALUE == islandInfo.getIslandAccessType()) {
            islandInfo.setSecret(request.getSecret().getValue());
        }
        if (request.hasShowIncome()) {
            islandInfo.setShowIncome(request.getShowIncome().getValue());
        }
        if (request.hasCustomUrl()) {
            if (this.islandInfoService.checkIslandCustomUrl(request.getCustomUrl().getValue())) {
                responseBuilder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_CUSTOM_URL_EXISTED_ERROR));
                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted();
            }
            islandInfo.setCustomUrl(request.getCustomUrl().getValue());
        }

        IslandInfo save = islandInfoService.updateIsland(islandInfo);
        IslandMessage islandMessage = islandInfoService.getIslandMessage(save);
        responseBuilder.setIsland(islandMessage).setStatus(CommonStatusUtils.getSuccStatus());
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * Implements the island's subscribers by island id method.
     *
     * @param request          {@link RetrieveIslandSubscribersByIdRequest}.
     * @param responseObserver {@link IslandSubscribersResponse}.
     */
    @Override
    public void retrieveIslandSubscribersById(RetrieveIslandSubscribersByIdRequest request, StreamObserver<IslandSubscribersResponse> responseObserver) {
        String islandId = request.getId();
        Pageable pageable = PageResponseUtil.getPageable(request.getPageRequest());
        //拿到分页之后的订阅者id
        Page<String> subscriberIdListPageable = subscriptionService.getSubscriberIdListByIslandId(islandId, pageable);
        List<String> subscriberIdList = subscriberIdListPageable.getContent();
        if (subscriberIdList.size() == 0) {
            responseObserver.onNext(IslandSubscribersResponse.newBuilder()
                    .addAllUser(Collections.emptyList())
                    .setStatus(CommonStatusUtils.getSuccStatus())
                    .build());
            responseObserver.onCompleted();
            return;
        }
        //根据idList拿到UserInfoList并转化为UserMessageList
        List<UserMessage> userMessageList = userInfoService.getUserMessageListByIdList(subscriberIdList);

        PageResponse pageResponse = PageResponseUtil.buildResponse(subscriberIdListPageable);
        IslandSubscribersResponse islandSubscribersResponse = IslandSubscribersResponse.newBuilder()
                .setPageResponse(pageResponse)
                .addAllUser(userMessageList)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(islandSubscribersResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements subscribe island by id method.
     *
     * @param request          {@link SubscribeIslandByIdRequest}.
     * @param responseObserver {@link SubscribeIslandResponse}.
     */
    @Override
    @Transactional
    public void subscribeIslandById(SubscribeIslandByIdRequest request, StreamObserver<SubscribeIslandResponse> responseObserver) {
        if (this.subscriptionService.isSubScribedIsland(request.getId(), request.getUserId())) {
            responseObserver.onNext(SubscribeIslandResponse.newBuilder()
                    .setStatus(CommonStatusUtils.getSuccStatus())
                    .build());
            responseObserver.onCompleted();
            return;
        }

        IslandInfo islandInfo = this.islandInfoService.findTopByIdAndDeletedIsFalse(request.getId());

        if (Objects.isNull(islandInfo)) {
            log.error("[subscribeIslandById] island not found error! island id is [{}]", request.getId());
            SubscribeIslandResponse response = SubscribeIslandResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NOT_FOUND_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        if (IslandAccessType.ISLAND_ACCESS_PRIVATE_VALUE == islandInfo.getIslandAccessType()
                && (!request.hasSecret() || !request.getSecret().getValue().equals(islandInfo.getSecret()))) {
            log.error("[subscribeIslandById] island secret error! island id is [{}], secret is [{}]", request.getId(), request.getSecret().getValue());
            SubscribeIslandResponse response = SubscribeIslandResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_SECRET_ERROR))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        Integer islanderNumber = this.islandInfoService.getLatestIslanderNumber(request.getId());
        try {
            this.subscriptionService.subscribeIsland(request.getId(), request.getUserId(), islandInfo.getHostId(), islanderNumber);
        } catch (KeepRealBusinessException exception) {
            SubscribeIslandResponse response = SubscribeIslandResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(exception.getErrorCode()))
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        SubscribeIslandResponse response = SubscribeIslandResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the unsubscribe island by id method.
     *
     * @param request          {@link UnsubscribeIslandByIdRequest}.
     * @param responseObserver {@link SubscribeIslandResponse}.
     */
    @Override
    public void unsubscribeIslandById(UnsubscribeIslandByIdRequest request, StreamObserver<SubscribeIslandResponse> responseObserver) {
        String islandId = request.getId();
        String userId = request.getUserId();
        this.subscriptionService.unsubscribeIsland(islandId, userId);

        SubscribeIslandResponse subscribeIslandResponse = SubscribeIslandResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus()).build();
        responseObserver.onNext(subscribeIslandResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the check new feeds method.
     *
     * @param request          {@link CheckNewFeedsRequest}.
     * @param responseObserver {@link CheckNewFeedsResponse}.
     */
    @Override
    public void checkNewFeeds(CheckNewFeedsRequest request, StreamObserver<CheckNewFeedsResponse> responseObserver) {
        List<String> islandIdList = request.getIslandIdsList();
        List<Long> timestampsList = request.getTimestampsList();
        List<CheckNewFeedsMessage> messageList = new ArrayList<>();
        if (islandIdList != null && timestampsList != null && islandIdList.size() > 0) {
            List<Map<String, Long>> resList = islandInfoService.findIslandIdAndLastFeedAtByIslandIdList(islandIdList);
            Map<String, Long> map = new HashMap<>();
            resList.forEach(m -> map.put(m.get("id").toString(), m.get("lastFeedAt")));
            for (int i = 0; i < islandIdList.size(); i++) {
                String islandId = islandIdList.get(i);
                CheckNewFeedsMessage feedMessage = islandInfoService.buildFeedMessage(islandId, map.get(islandId), timestampsList.get(i));
                messageList.add(feedMessage);
            }
        }
        CheckNewFeedsResponse response = CheckNewFeedsResponse.newBuilder()
                .addAllCheckNewFeeds(messageList)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the update lastFeedAt by id method.
     *
     * @param request          {@link UpdateLastFeedAtRequest}.
     * @param responseObserver {@link UpdateLastFeedAtResponse}.
     */
    @Override
    public void updateLastFeedAtById(UpdateLastFeedAtRequest request, StreamObserver<UpdateLastFeedAtResponse> responseObserver) {
        List<String> islandIdList = request.getIslandIdsList();
        long timestamps = request.getTimestamps();
        this.islandInfoService.updateLastFeedAtByIslandIdList(islandIdList, timestamps);

        UpdateLastFeedAtResponse response = UpdateLastFeedAtResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the default islands method.
     *
     * @param request          {@link RetrieveDefaultIslandsByUserIdRequest}.
     * @param responseObserver {@link IslandsResponse}.
     */
    @Override
    public void retrieveDefaultIslandsByUserId(RetrieveDefaultIslandsByUserIdRequest request, StreamObserver<IslandsResponse> responseObserver) {
        String userId = request.getUserId();
        Pageable pageable = PageResponseUtil.getPageable(request.getPageRequest());
        IslandsResponse.Builder builder = IslandsResponse.newBuilder();

        List<IslandInfo> islandInfoList = this.islandInfoService.getIslandBySubscribed(userId, pageable, builder);

        if (request.hasIslandId()) {
            String islandId = request.getIslandId().getValue();
            IslandInfo island = this.islandInfoService.findTopByIdAndDeletedIsFalse(islandId);
            if (island == null || !islandInfoList.contains(island)) {
                log.error("[retrieveDefaultIslandsByUserId] island not found error! island id is [{}]", islandId);
                responseObserver.onNext(IslandsResponse.newBuilder()
                        .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NOT_FOUND_ERROR))
                        .build());
                responseObserver.onCompleted();
                return;
            }
            islandInfoList.remove(island);
            islandInfoList.add(0, island);
        }
        builder.addAllIslands(islandInfoList.stream().map(this.islandInfoService::getIslandMessage).filter(Objects::nonNull).collect(Collectors.toList()));
        builder.setStatus(CommonStatusUtils.getSuccStatus());
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveUserSubscriptionState(RetrieveUserSubscriptionStateRequest request, StreamObserver<RetrieveUserSubscriptionStateResponse> responseObserver) {
        String userId = request.getUserId();
        ProtocolStringList islandIdsList = request.getIslandIdsList();
        Map<String, Boolean> stateMap = new HashMap<>();
        islandIdsList.forEach(id -> stateMap.put(id, false));

        // Updates by user subscription
        List<String> islandIdList = subscriptionService.getSubscribeIslandIdByUserId(userId, islandIdsList);
        islandIdList.forEach(id -> stateMap.put(id, true));

        // Updates by island type
        List<IslandInfo> islandInfos = this.islandInfoService.retrieveByIslandIds(islandIdsList);
        islandInfos.stream()
                .filter(islandInfo -> islandInfo.getIslandAccessType() == IslandAccessType.ISLAND_ACCESS_PUBLIC_VALUE)
                .map(IslandInfo::getId)
                .forEach(id -> stateMap.put(id, true));
        
        RetrieveUserSubscriptionStateResponse response = RetrieveUserSubscriptionStateResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .putAllStateMap(stateMap)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveDeviceTokensById(RetrieveDeviceTokensRequest request, StreamObserver<RetrieveDeviceTokensResponse> responseObserver) {
        String islandId = request.getIslandId();
        Pageable pageable = PageResponseUtil.getPageable(request.getPageRequest());

        Page<String> subscriberIdsPageable = subscriptionService.getIslanderIdListByIslandId(islandId, pageable);

        List<String> androidTokenList = new ArrayList<>();
        List<String> iosTokenList = new ArrayList<>();
        userDeviceInfoService.getDeviceTokenListByUserIdList(subscriberIdsPageable.getContent())
                .forEach(info -> {
                    if (info.getDeviceType().equals(DeviceType.ANDROID_VALUE)) {
                        androidTokenList.add(info.getDeviceToken());
                    } else {
                        iosTokenList.add(info.getDeviceToken());
                    }
                });

        RetrieveDeviceTokensResponse response = RetrieveDeviceTokensResponse.newBuilder()
                .addAllAndroidTokens(androidTokenList)
                .addAllIosTokens(iosTokenList)
                .setPageResponse(PageResponseUtil.buildResponse(subscriberIdsPageable))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Dismisses the introduction.
     *
     * @param request          {@link DismissIntroductionRequest}.
     * @param responseObserver {@link CommonStatus}.
     */
    @Override
    public void dismissIntroduction(DismissIntroductionRequest request,
                                    StreamObserver<CommonStatus> responseObserver) {

        this.subscriptionService.dismissHostAndUserIntroduction(request.getUserId(), request.getIslandId());

        responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveIslanderPortraitUrlByIslandId(RetrieveIslanderPortraitUrlRequest request, StreamObserver<RetrieveIslanderPortraitUrlResponse> responseObserver) {
        String islandId = request.getIslandId();
        Page<String> userIdList = this.subscriptionService.getSubscriberIdListByIslandId(islandId, PageRequest.of(0, 6));
        List<UserInfo> userInfoList = this.userInfoService.findUserInfosByIds(userIdList.getContent());

        responseObserver.onNext(RetrieveIslanderPortraitUrlResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllPortraitUrl(userInfoList.stream().map(UserInfo::getPortraitImageUri).filter(uri -> !StringUtils.isEmpty(uri)).limit(3).collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * Checks island subscription state.
     *
     * @param request          {@link CheckIslandSubscriptionRequest}.
     * @param responseObserver {@link IslandSubscriptionStateResponse}.
     */
    @Override
    public void checkIslandSubscription(CheckIslandSubscriptionRequest request,
                                        StreamObserver<IslandSubscriptionStateResponse> responseObserver) {
        Subscription subscription = this.subscriptionService.getSubscriptionByIslandIdAndUserId(request.getIslandId(), request.getUserId());

        IslandSubscriptionStateResponse.Builder responseBuilder = IslandSubscriptionStateResponse.newBuilder();
        responseBuilder.setHasSubscribed(!Objects.isNull(subscription)
                && (SubscriptionState.ISLANDER.getValue() == (subscription.getState()) || SubscriptionState.HOST.getValue() == subscription.getState()));

        responseBuilder.setStatus(CommonStatusUtils.getSuccStatus());
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * Retrieves all islands in discovery.
     *
     * @param request          {@link DiscoverIslandsRequest}.
     * @param responseObserver {@link DiscoverIslandsResponse}.
     */
    @Override
    public void discoverIslands(DiscoverIslandsRequest request,
                                StreamObserver<DiscoverIslandsResponse> responseObserver) {
        List<DiscoverIslandMessage> discoverIslandMessageList = this.islandInfoService.retrieveAllDiscoveredIslands(request.getIsAuditMode());

        DiscoverIslandsResponse response = DiscoverIslandsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllDicoverIslands(discoverIslandMessageList)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createOrUpdateSupportTarget(CreateOrUpdateSupportTargetRequest request, StreamObserver<SupportTargetResponse> responseObserver) {
        SupportTarget supportTarget;
        SupportTargetResponse.Builder responseBuilder = SupportTargetResponse.newBuilder();

        if (!request.hasId()) {
            SupportTarget.SupportTargetBuilder builder = SupportTarget.builder()
                    .hostId(request.getHostId())
                    .islandId(request.getIslandId())
                    .timeType(request.getTimeType().getValue())
                    .content(request.getContent().getValue())
                    .targetType(request.getTargetType().getValue());

            switch (this.islandInfoService.convertToTargetType(request.getTargetType().getValue())) {
                case SUPPORTER:
                    builder.totalSupporterNum(request.getTotalSupporterNum().getValue());
                    break;
                case AMOUNT:
                    builder.totalAmountInCents(request.getTotalAmountInCents().getValue());
                    break;
                case UNRECOGNIZED:
                    log.error("unknown support target type islandId is {}, targetId is {}", request.getIslandId(),
                            StringUtils.isEmpty(request.getId().getValue()) ? "no target id" : request.getId());
                    break;
            }
            supportTarget = this.islandInfoService.createSupportTarget(builder.build());
        } else {
            supportTarget = this.islandInfoService.findSupportTargetByIdAndDeletedIsFalse(request.getId().getValue());
            if (Objects.isNull(supportTarget)) {
                log.error("retrieve supportTarget return null and id is {}", request.getId());
                responseBuilder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUPPORT_TARGET_NOT_FOUND_ERROR));
                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted();
            }

            if (request.hasTargetType() && supportTarget.getTargetType() != request.getTargetType().getValue()) {
                supportTarget.setDeleted(true);
                this.islandInfoService.updateSupportTarget(supportTarget);
                SupportTarget supportTargetNew = new SupportTarget();
                supportTargetNew.setTargetType(request.getTargetType().getValue());
                supportTargetNew.setIslandId(request.getIslandId());
                supportTargetNew.setHostId(request.getHostId());
                supportTargetNew.setTimeType(request.getTimeType().getValue());
                supportTarget = this.islandInfoService.createSupportTarget(supportTargetNew);
            }

            if (request.hasTimeType()) {
                supportTarget.setTimeType(request.getTimeType().getValue());
            }

            if (request.hasContent()) {
                supportTarget.setContent(request.getContent().getValue());
            }

            if (request.hasTotalAmountInCents()) {
                supportTarget.setTotalAmountInCents(request.getTotalAmountInCents().getValue());
            }

            if (request.hasTotalSupporterNum()) {
                supportTarget.setTotalSupporterNum(request.getTotalSupporterNum().getValue());
            }

            supportTarget = this.islandInfoService.updateSupportTarget(supportTarget);
        }

        SupportTargetResponse response = SupportTargetResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setSupportTarget(this.islandInfoService.getSupportTargetMessage(supportTarget))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 删除支持目标
     *
     * @param request {@link DeleteSupportTargetRequest}
     * @param responseObserver {@link CommonStatus}
     */
    @Override
    public void deleteSupportTarget(DeleteSupportTargetRequest request, StreamObserver<CommonStatus> responseObserver) {
        SupportTarget supportTarget = this.islandInfoService.findSupportTargetByIdAndDeletedIsFalse(request.getId());
        if (Objects.isNull(supportTarget) || !request.getHostId().equals(supportTarget.getHostId())) {
            responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_FORBIDDEN));
            responseObserver.onCompleted();
        }
        supportTarget.setDeleted(true);
        this.islandInfoService.updateSupportTarget(supportTarget);
        responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
        responseObserver.onCompleted();
    }

    /**
     * 获取支持目标
     *
     * @param request {@link RetrieveSupportTargetsRequest}
     * @param responseObserver {@link SupportTargetsResponse}
     */
    @Override
    public void retrieveSupportTargets(RetrieveSupportTargetsRequest request, StreamObserver<SupportTargetsResponse> responseObserver) {
        List<SupportTarget> list = this.islandInfoService.findAllSupportTargetByIslandId(request.getIslandId());
        SupportTargetsResponse response = SupportTargetsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllSupportTargets(list.stream().map(this.islandInfoService::getSupportTargetMessage).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
