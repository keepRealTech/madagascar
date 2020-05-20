package com.keepreal.madagascar.coua.grpcController;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CheckNameRequest;
import com.keepreal.madagascar.coua.CheckNameResponse;
import com.keepreal.madagascar.coua.CheckNewFeedsMessage;
import com.keepreal.madagascar.coua.CheckNewFeedsRequest;
import com.keepreal.madagascar.coua.CheckNewFeedsResponse;
import com.keepreal.madagascar.coua.IslandProfileResponse;
import com.keepreal.madagascar.coua.IslandResponse;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.IslandSubscribersResponse;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.NewIslandRequest;
import com.keepreal.madagascar.coua.QueryIslandCondition;
import com.keepreal.madagascar.coua.RetrieveDefaultIslandsByUserIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandProfileByIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandSubscribersByIdRequest;
import com.keepreal.madagascar.coua.RetrieveMultipleIslandsRequest;
import com.keepreal.madagascar.coua.SubscribeIslandByIdRequest;
import com.keepreal.madagascar.coua.SubscribeIslandResponse;
import com.keepreal.madagascar.coua.UnsubscribeIslandByIdRequest;
import com.keepreal.madagascar.coua.UpdateIslandByIdRequest;
import com.keepreal.madagascar.coua.UpdateLastFeedAtRequest;
import com.keepreal.madagascar.coua.UpdateLastFeedAtResponse;
import com.keepreal.madagascar.coua.model.IslandInfo;
import com.keepreal.madagascar.coua.service.FeedService;
import com.keepreal.madagascar.coua.service.IslandInfoService;
import com.keepreal.madagascar.coua.service.SubscriptionService;
import com.keepreal.madagascar.coua.service.UserInfoService;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import com.keepreal.madagascar.coua.util.PageResponseUtil;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    /**
     * Constructs the island grpc controller.
     *
     * @param islandInfoService     {@link IslandInfoService}.
     * @param subscriptionService   {@link SubscriptionService}.
     * @param feedService           {@link FeedService}.
     * @param userInfoService       {@link UserInfoService}.
     */
    public IslandGRpcController(IslandInfoService islandInfoService,
                                SubscriptionService subscriptionService,
                                FeedService feedService,
                                UserInfoService userInfoService) {
        this.islandInfoService = islandInfoService;
        this.subscriptionService = subscriptionService;
        this.feedService = feedService;
        this.userInfoService = userInfoService;
    }

    /**
     * Implements the check island name method.
     *
     * @param request           {@link CheckNameRequest}.
     * @param responseObserver  {@link CheckNameResponse}.
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
     * @param request           {@link NewIslandRequest}.
     * @param responseObserver  {@link IslandResponse}.
     */
    @Override
    public void createIsland(NewIslandRequest request, StreamObserver<IslandResponse> responseObserver) {
        if (islandInfoService.islandNameIsExisted(request.getName())) {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NAME_EXISTED_ERROR);
            responseObserver.onNext(IslandResponse.newBuilder().setStatus(commonStatus).build());
            responseObserver.onCompleted();
            return;
        }
        IslandInfo.IslandInfoBuilder infoBuilder = IslandInfo.builder()
                .hostId(request.getHostId())
                .islandName(request.getName());
        if (request.hasPortraitImageUri()) {
            infoBuilder.portraitImageUri(request.getPortraitImageUri().getValue());
        }
        if (request.hasSecret()) {
            infoBuilder.secret(request.getSecret().getValue());
        }

        IslandInfo save;
        try {
            save = islandInfoService.createIsland(infoBuilder.build());
            feedService.createDefaultFeed(request.getHostId(), request.getHostId(), save.getId());
        } catch (KeepRealBusinessException e) {
            if (e.getErrorCode().equals(ErrorCode.REQUEST_UNEXPECTED_ERROR))
                log.error("rpc call fossa error");
            responseObserver.onNext(IslandResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(e.getErrorCode()))
                    .build());
            responseObserver.onCompleted();
            return;
        }

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
     * @param request           {@link RetrieveIslandByIdRequest}.
     * @param responseObserver  {@link IslandResponse}.
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
     * @param request           {@link RetrieveIslandProfileByIdRequest}.
     * @param responseObserver  {@link IslandProfileResponse}.
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
                Integer userIndex = subscriptionService.getUserIndexByIslandId(islandInfo.getId(), request.getUserId());
                responseBuilder.setIsland(islandMessage)
                        .setHost(userMessage)
                        .setUserIndex(StringValue.of(userIndex == null ? "" : userIndex.toString()))
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
     * @param request           {@link RetrieveMultipleIslandsRequest} (hostId, subscriberId, islandName)
     * @param responseObserver  {@link IslandsResponse}.
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
                islandInfoList = islandInfoService.getIslandByName(requestCondition.getName().getValue());
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
     * @param request           {@link UpdateIslandByIdRequest}.
     * @param responseObserver  {@link IslandResponse}.
     */
    @Override
    public void updateIslandById(UpdateIslandByIdRequest request, StreamObserver<IslandResponse> responseObserver) {
        IslandInfo islandInfo = islandInfoService.findTopByIdAndDeletedIsFalse(request.getId());
        IslandResponse.Builder responseBuilder = IslandResponse.newBuilder();
        if (islandInfo != null) {
            if (request.hasName() && !islandInfoService.islandNameIsExisted(request.getName().getValue())) {
                islandInfo.setIslandName(request.getName().getValue());
            }
            if (request.hasDescription()) {
                islandInfo.setDescription(request.getDescription().getValue());
            }
            if (request.hasPortraitImageUri()) {
                islandInfo.setPortraitImageUri(request.getPortraitImageUri().getValue());
            }
            if (request.hasSecret()) {
                islandInfo.setSecret(request.getSecret().getValue());
            }
            IslandInfo save = islandInfoService.updateIsland(islandInfo);
            IslandMessage islandMessage = islandInfoService.getIslandMessage(save);
            responseBuilder.setIsland(islandMessage).setStatus(CommonStatusUtils.getSuccStatus());
        } else {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
        }

        IslandResponse islandResponse = responseBuilder.build();
        responseObserver.onNext(islandResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the island's subscribers by island id method.
     *
     * @param request           {@link RetrieveIslandSubscribersByIdRequest}.
     * @param responseObserver  {@link IslandSubscribersResponse}.
     */
    @Override
    public void retrieveIslandSubscribersById(RetrieveIslandSubscribersByIdRequest request, StreamObserver<IslandSubscribersResponse> responseObserver) {
        String islandId = request.getId();
        Pageable pageable = PageResponseUtil.getPageable(request.getPageRequest());
        //拿到分页之后的订阅者id
        Page<String> subscriberIdListPageable = subscriptionService.getSubscriberIdListByIslandId(islandId, pageable);
        List<String> subscriberIdList = subscriberIdListPageable.getContent();
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
     * @param request           {@link SubscribeIslandByIdRequest}.
     * @param responseObserver  {@link SubscribeIslandResponse}.
     */
    @Override
    @Transactional
    public void subscribeIslandById(SubscribeIslandByIdRequest request, StreamObserver<SubscribeIslandResponse> responseObserver) {
        String islandId = request.getId();
        String secret = request.getSecret();
        String userId = request.getUserId();
        SubscribeIslandResponse.Builder responseBuilder = SubscribeIslandResponse.newBuilder();
        IslandInfo islandInfo = islandInfoService.findTopByIdAndDeletedIsFalse(islandId);
        if (islandInfo != null) {
            if (secret.equals(islandInfo.getSecret())) {
                Integer islanderNumber = islandInfoService.getLatestIslanderNumber(islandId);
                try {
                    subscriptionService.subscribeIsland(islandId, userId, islandInfo.getHostId(), islanderNumber);
                } catch (KeepRealBusinessException e) {
                    CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(e.getErrorCode());
                    responseBuilder.setStatus(commonStatus);
                    responseObserver.onNext(responseBuilder.build());
                    responseObserver.onCompleted();
                    return;
                }
            } else {
                CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_SECRET_ERROR);
                responseBuilder.setStatus(commonStatus);
                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted();
                return;
            }
        } else {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
        }

        SubscribeIslandResponse subscribeIslandResponse = SubscribeIslandResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(subscribeIslandResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the unsubscribe island by id method.
     *
     * @param request           {@link UnsubscribeIslandByIdRequest}.
     * @param responseObserver  {@link SubscribeIslandResponse}.
     */
    @Override
    public void unsubscribeIslandById(UnsubscribeIslandByIdRequest request, StreamObserver<SubscribeIslandResponse> responseObserver) {
        String islandId = request.getId();
        String userId = request.getUserId();
        subscriptionService.unsubscribeIsland(islandId, userId);

        SubscribeIslandResponse subscribeIslandResponse = SubscribeIslandResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus()).build();
        responseObserver.onNext(subscribeIslandResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements the check new feeds method.
     *
     * @param request           {@link CheckNewFeedsRequest}.
     * @param responseObserver  {@link CheckNewFeedsResponse}.
     */
    @Override
    public void checkNewFeeds(CheckNewFeedsRequest request, StreamObserver<CheckNewFeedsResponse> responseObserver) {
        List<String> islandIdList = request.getIslandIdsList();
        List<Long> timestampsList = request.getTimestampsList();
        List<CheckNewFeedsMessage> messageList = new ArrayList<>();
        if (islandIdList.size() == timestampsList.size()) {
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
     * @param request           {@link UpdateLastFeedAtRequest}.
     * @param responseObserver  {@link UpdateLastFeedAtResponse}.
     */
    @Override
    public void updateLastFeedAtById(UpdateLastFeedAtRequest request, StreamObserver<UpdateLastFeedAtResponse> responseObserver) {
        List<String> islandIdList = request.getIslandIdsList();
        long timestamps = request.getTimestamps();
        islandInfoService.updateLastFeedAtByIslandIdList(islandIdList, timestamps);

        UpdateLastFeedAtResponse response = UpdateLastFeedAtResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 根据userId获取该用户创建和加入的岛，按规则排序
     *  - 用户创建的岛在最前面
     *  - 如果该用户没有创建岛，最新发布动态的岛在前面
     *  - 如果都没有，最新加入的岛在前面
     *
     * @param request           {@link RetrieveDefaultIslandsByUserIdRequest}.
     * @param responseObserver  {@link IslandsResponse}.
     */
    @Override
    public void retrieveDefaultIslandsByUserId(RetrieveDefaultIslandsByUserIdRequest request, StreamObserver<IslandsResponse> responseObserver) {
        String userId = request.getUserId();
        Pageable pageable = PageResponseUtil.getPageable(request.getPageRequest());
        IslandsResponse.Builder builder = IslandsResponse.newBuilder();

        List<IslandInfo> islandInfoList = islandInfoService.getIslandBySubscribed(userId, pageable, builder);
        if (islandInfoList.size() > 0) {
            if (request.hasIslandId()) {
                String islandId = request.getIslandId().getValue();
                IslandInfo island = islandInfoService.findTopByIdAndDeletedIsFalse(islandId);
                islandInfoList = islandInfoList.stream().filter(info -> !islandId.equals(info.getId())).collect(Collectors.toList());
                islandInfoList.add(0, island);
            } else {
                String islandId;
                IslandInfo islandInfo = islandInfoList.get(0);
                if (!userId.equals(islandInfo.getHostId()) && (islandId = feedService.retrieveLatestFeedByUserIdGetIslandId(userId)) != null) {
                    IslandInfo island = islandInfoService.findTopByIdAndDeletedIsFalse(islandId);
                    if (island != null) {
                        islandInfoList = islandInfoList.stream().filter(info -> !islandId.equals(info.getId())).collect(Collectors.toList());
                        islandInfoList.add(0, island);
                    }
                }
            }
            builder.setStatus(CommonStatusUtils.getSuccStatus())
                    .addAllIslands(islandInfoList.stream().map(islandInfoService::getIslandMessage).filter(Objects::nonNull).collect(Collectors.toList()))
                    .build();
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onNext(IslandsResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_UNEXPECTED_ERROR))
                    .build());
            responseObserver.onCompleted();
        }
    }
}
