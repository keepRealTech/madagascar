package com.keepreal.madagascar.coua.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
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
import com.keepreal.madagascar.coua.dao.IslandInfoRepository;
import com.keepreal.madagascar.coua.model.IslandInfo;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import com.keepreal.madagascar.coua.util.PageResponseUtil;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/
@GRpcService
public class IslandInfoService extends IslandServiceGrpc.IslandServiceImplBase {

    private final IslandInfoRepository islandInfoRepository;
    private final SubscriptionService subscriptionService;
    private final UserInfoService userInfoService;
    private final LongIdGenerator idGenerator;

    @Autowired
    public IslandInfoService(IslandInfoRepository islandInfoRepository, SubscriptionService subscriptionService, UserInfoService userInfoService, LongIdGenerator idGenerator) {
        this.islandInfoRepository = islandInfoRepository;
        this.subscriptionService = subscriptionService;
        this.userInfoService = userInfoService;
        this.idGenerator = idGenerator;
    }

    /**
     * 检查island name 是否已经存在
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void checkName(CheckNameRequest request, StreamObserver<CheckNameResponse> responseObserver) {
        String islandName = request.getName();
        boolean isExisted = islandNameIsExisted(islandName);
        CheckNameResponse checkNameResponse = CheckNameResponse.newBuilder()
                .setIsExisted(isExisted)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(checkNameResponse);
        responseObserver.onCompleted();
    }

    /**
     * 创建island
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void createIsland(NewIslandRequest request, StreamObserver<IslandResponse> responseObserver) {
        if (islandNameIsExisted(request.getName())) {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_NAME_EXISTED_ERROR);
            responseObserver.onNext(IslandResponse.newBuilder().setStatus(commonStatus).build());
            responseObserver.onCompleted();
            return;
        }
        String islandId = String.valueOf(idGenerator.nextId());
        IslandInfo.IslandInfoBuilder infoBuilder = IslandInfo.builder()
                .id(islandId)
                .hostId(request.getHostId())
                .islandName(request.getName())
                .lastFeedAt(System.currentTimeMillis());
        if (request.hasPortraitImageUri()) {
            infoBuilder.portraitImageUri(request.getPortraitImageUri().getValue());
        }
        if (request.hasSecret()) {
            infoBuilder.secret(request.getSecret().getValue());
        }
        // 将数据插入 island 表
        IslandInfo save = islandInfoRepository.save(infoBuilder.build());
        // 维护 subscription 表
        String hostId = request.getHostId();
        subscriptionService.initHost(islandId, hostId);

        IslandMessage islandMessage = getIslandMessage(save);
        IslandResponse islandResponse = IslandResponse.newBuilder()
                .setIsland(islandMessage)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(islandResponse);
        responseObserver.onCompleted();
    }

    /**
     * 根据 island id 查询岛的信息
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveIslandById(RetrieveIslandByIdRequest request, StreamObserver<IslandResponse> responseObserver) {
        IslandResponse.Builder responseBuilder = IslandResponse.newBuilder();
        String islandId = request.getId();
        IslandInfo islandInfo = islandInfoRepository.findTopByIdAndDeletedIsFalse(islandId);
        if (islandInfo != null) {
            IslandMessage islandMessage = getIslandMessage(islandInfo);
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
     * 根据条件查询符合条件的岛的列表
     *
     * @param request          可根据 name，hostId，subscriberId 查询
     * @param responseObserver
     */
    @Override
    public void retrieveIslandsByCondition(RetrieveMultipleIslandsRequest request, StreamObserver<IslandsResponse> responseObserver) {
        List<IslandInfo> islandInfoList;
        PageRequest pageRequest = request.getPageRequest();
        int page = pageRequest.getPage();
        int pageSize = pageRequest.getPageSize();
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);
        IslandsResponse.Builder builder = IslandsResponse.newBuilder();

        QueryIslandCondition requestCondition = request.getCondition();
        if (requestCondition.hasHostId()) {
            islandInfoList = getMyCreatedIsland(requestCondition.getHostId().getValue(), pageable, builder);
        } else {
            boolean hasName = requestCondition.hasName();
            boolean hasSubscribedUserId = requestCondition.hasSubscribedUserId();
            if (hasName && hasSubscribedUserId) {
                islandInfoList = getIslandByNameAndSubscribed(requestCondition.getName().getValue(), requestCondition.getSubscribedUserId().getValue());
            } else if (hasSubscribedUserId) {
                islandInfoList = getIslandBySubscribed(requestCondition.getSubscribedUserId().getValue(), pageable, builder);
            } else if (hasName) {
                islandInfoList = getIslandByName(requestCondition.getName().getValue());
            } else {
                islandInfoList = getIsland(pageable, builder);
            }
        }

        List<IslandMessage> islandMessageList = islandInfoList.stream().map(this::getIslandMessage).filter(Objects::nonNull).collect(Collectors.toList());
        builder.addAllIslands(islandMessageList);

        IslandsResponse islandsResponse = builder.setStatus(CommonStatusUtils.getSuccStatus()).build();
        responseObserver.onNext(islandsResponse);
        responseObserver.onCompleted();
    }

    /**
     * 通过 id 更新 island 的信息，部分更新，先查后改
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void updateIslandById(UpdateIslandByIdRequest request, StreamObserver<IslandResponse> responseObserver) {
        Optional<IslandInfo> optionalIslandInfo = islandInfoRepository.findById(request.getId());
        IslandResponse.Builder responseBuilder = IslandResponse.newBuilder();
        if (optionalIslandInfo.isPresent()) {
            IslandInfo islandInfo = optionalIslandInfo.get();
            if (request.hasName() && !islandNameIsExisted(request.getName().getValue())) {
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
            IslandInfo save = islandInfoRepository.save(islandInfo);
            IslandMessage islandMessage = getIslandMessage(save);
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
     * 根据 id 查询 island 详情页
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveIslandProfileById(RetrieveIslandProfileByIdRequest request, StreamObserver<IslandProfileResponse> responseObserver) {
        boolean islandFound = false;
        boolean userFound = false;
        IslandProfileResponse.Builder responseBuilder = IslandProfileResponse.newBuilder();
        Optional<IslandInfo> islandInfoOptional = islandInfoRepository.findById(request.getId());
        if (islandInfoOptional.isPresent()) {
            islandFound = true;
            IslandInfo islandInfo = islandInfoOptional.get();
            UserMessage userMessage = userInfoService.getUserMessageById(islandInfo.getHostId());
            if (userMessage != null) {
                userFound = true;
                IslandMessage islandMessage = getIslandMessage(islandInfo);
                Integer userIndex = subscriptionService.getUserIndexByIslandId(islandInfo.getId(), islandInfo.getHostId());
                responseBuilder.setIsland(islandMessage)
                        .setHost(userMessage)
                        .setUserIndex(StringValue.newBuilder().setValue(userIndex.toString()).build())
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
     * 根据 island id 返回岛的所有订阅者信息
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveIslandSubscribersById(RetrieveIslandSubscribersByIdRequest request, StreamObserver<IslandSubscribersResponse> responseObserver) {
        String islandId = request.getId();
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);
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
     * 订阅一个岛
     *
     * @param request
     * @param responseObserver
     */
    @Override
    @Transactional
    public void subscribeIslandById(SubscribeIslandByIdRequest request, StreamObserver<SubscribeIslandResponse> responseObserver) {
        String islandId = request.getId();
        String secret = request.getSecret();
        String userId = request.getUserId();
        SubscribeIslandResponse.Builder responseBuilder = SubscribeIslandResponse.newBuilder();
        Optional<IslandInfo> islandInfoOptional = islandInfoRepository.findById(islandId);
        if (islandInfoOptional.isPresent()) {
            IslandInfo islandInfo = islandInfoOptional.get();
            if (secret.equals(islandInfo.getSecret())) {
                Integer islanderNumber = islandInfoRepository.getIslanderNumberByIslandId(islandId);
                subscriptionService.subscribeIsland(islandId, userId, islandInfo.getHostId(), islanderNumber);
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
     * 取消订阅这个岛
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void unsubscribeIslandById(UnsubscribeIslandByIdRequest request, StreamObserver<SubscribeIslandResponse> responseObserver) {
        String islandId = request.getId();
        String userId = request.getUserId();
        subscriptionService.unSubscribeIsland(islandId, userId);

        SubscribeIslandResponse subscribeIslandResponse = SubscribeIslandResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus()).build();
        responseObserver.onNext(subscribeIslandResponse);
        responseObserver.onCompleted();
    }

    /**
     * 根据islandId和对应的timestamp检查是否有新的未读feed
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void checkNewFeeds(CheckNewFeedsRequest request, StreamObserver<CheckNewFeedsResponse> responseObserver) {
        List<String> islandIdList = request.getIslandIdsList();
        List<Long> timestampsList = request.getTimestampsList();
        List<CheckNewFeedsMessage> messageList = new ArrayList<>();
        if (islandIdList.size() == timestampsList.size()) {
            List<Map<String, Long>> resList = islandInfoRepository.findIslandIdAndLastFeedAtByIslandIdList(islandIdList);
            Map<String, Long> map = new HashMap<>();
            resList.forEach(m -> map.put(m.get("id").toString(), m.get("lastFeedAt")));
            for (int i = 0; i < islandIdList.size(); i++) {
                String islandId = islandIdList.get(i);
                CheckNewFeedsMessage feedMessage = getFeedMessage(islandId, map.get(islandId), timestampsList.get(i));
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
     * 根据islandId和timestamp更新island的lastFeedAt
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void updateLastFeedAtById(UpdateLastFeedAtRequest request, StreamObserver<UpdateLastFeedAtResponse> responseObserver) {
        List<String> islandIdList = request.getIslandIdsList();
        long timestamps = request.getTimestamps();
        islandInfoRepository.updateLastFeedAtByIslandIdList(islandIdList, timestamps);

        UpdateLastFeedAtResponse response = UpdateLastFeedAtResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 拿到最新的islanderNumber(岛成员编号)
     *
     * @param islandId
     * @return
     */
    public Integer getLatestIslanderNumber(String islandId) {
        return islandInfoRepository.getIslanderNumberByIslandId(islandId);
    }

    private boolean islandNameIsExisted(String islandName) {
        return islandInfoRepository.findTopByIslandNameAndDeletedIsFalse(islandName) != null;
    }

    /**
     * 把 IslandInfo 对象包装为 IslandMessage 对象
     *
     * @param islandInfo
     * @return
     */
    private IslandMessage getIslandMessage(IslandInfo islandInfo) {
        if (islandInfo == null) {
            return null;
        }
        Integer memberCount = subscriptionService.getMemberCountByIslandId(islandInfo.getId());
        return IslandMessage
                .newBuilder()
                .setId(islandInfo.getId())
                .setName(islandInfo.getIslandName())
                .setHostId(islandInfo.getHostId())
                .setPortraitImageUri(islandInfo.getPortraitImageUri())
                .setDescription(islandInfo.getDescription())
                .setLastFeedAt(islandInfo.getLastFeedAt())
                .setCreatedAt(islandInfo.getCreatedTime())
                .setSecret(islandInfo.getSecret())
                .setMemberCount(memberCount)
                .build();
    }

    private CheckNewFeedsMessage getFeedMessage(String islandId, Long islandLastFeedAt, Long currentTime) {
        return CheckNewFeedsMessage.newBuilder()
                .setIslandId(islandId)
                .setHasNewFeeds(islandLastFeedAt != null && islandLastFeedAt > currentTime)
                .build();
    }

    /**
     * 用户创建的岛
     * @param userId
     * @param pageable
     * @param builder
     * @return
     */
    private List<IslandInfo> getMyCreatedIsland(String userId, Pageable pageable, IslandsResponse.Builder builder) {
        Page<String> islandIdListPageable = subscriptionService.getIslandIdListByUserCreated(userId, pageable);
        builder.setPageResponse(PageResponseUtil.buildResponse(islandIdListPageable));
        return islandInfoRepository.findIslandInfosByIdInAndDeletedIsFalse(islandIdListPageable.getContent());
    }

    private List<IslandInfo> getIslandByName(String islandName) {
        IslandInfo islandInfo = islandInfoRepository.findTopByIslandNameAndDeletedIsFalse(islandName);
        if (islandInfo == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(islandInfo);
    }

    private List<IslandInfo> getIslandByNameAndSubscribed(String islandName, String userId) {
        IslandInfo islandInfo = islandInfoRepository.findTopByIslandNameAndDeletedIsFalse(islandName);
        if (islandInfo == null || !subscriptionService.isSubScribedIsland(islandInfo.getId(), userId)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(islandInfo);
    }

    private List<IslandInfo> getIslandBySubscribed(String userId, Pageable pageable, IslandsResponse.Builder builder) {
        Page<String> islandIdListPageable = subscriptionService.getIslandIdListByUserSubscribed(userId, pageable);
        builder.setPageResponse(PageResponseUtil.buildResponse(islandIdListPageable));
        return islandInfoRepository.findIslandInfosByIdInAndDeletedIsFalse(islandIdListPageable.getContent());
    }

    private List<IslandInfo> getIsland(Pageable pageable, IslandsResponse.Builder builder) {
        Page<IslandInfo> islandInfoListPageable = islandInfoRepository.findAllByDeletedIsFalse(pageable);
        builder.setPageResponse(PageResponseUtil.buildResponse(islandInfoListPageable));
        return islandInfoListPageable.getContent();
    }
}
