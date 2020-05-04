package com.keepreal.madagascar.coua.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.*;
import com.keepreal.madagascar.coua.dao.IslandInfoRepository;
import com.keepreal.madagascar.coua.model.IslandInfo;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Autowired
    public IslandInfoService(IslandInfoRepository islandInfoRepository, SubscriptionService subscriptionService, UserInfoService userInfoService) {
        this.islandInfoRepository = islandInfoRepository;
        this.subscriptionService = subscriptionService;
        this.userInfoService = userInfoService;
    }

    /**
     * 检查island name 是否已经存在
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
     * @param request
     * @param responseObserver
     */
    @Override
    public void createIsland(NewIslandRequest request, StreamObserver<IslandResponse> responseObserver) {
        Long islandId = 0L; //todo: 从id生成器中获取
        IslandInfo islandInfo = new IslandInfo();
        islandInfo.setId(islandId);
        islandInfo.setHostId(Long.valueOf(request.getHostId()));
        islandInfo.setIslandName(request.getName()); //todo: 是否需要再校验一遍
        if (request.hasPortraitImageUri()) {
            islandInfo.setPortraitImageUri(request.getPortraitImageUri().getValue());
        }
        if (request.hasSecret()) {
            islandInfo.setSecret(request.getSecret().getValue());
        }
        islandInfo.setLastFeedAt(System.currentTimeMillis());
        // 将数据插入 island 表
        IslandInfo save = islandInfoRepository.save(islandInfo);
        // 维护 subscription 表
        String hostId = request.getHostId();
        subscriptionService.initHost(islandId, Long.valueOf(hostId));

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
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveIslandById(RetrieveIslandByIdRequest request, StreamObserver<IslandResponse> responseObserver) {
        IslandResponse.Builder responseBuilder = IslandResponse.newBuilder();
        String islandId = request.getId();
        Optional<IslandInfo> islandInfoOptional = islandInfoRepository.findById(Long.valueOf(islandId));
        if (islandInfoOptional.isPresent()) {
            IslandInfo islandInfo = islandInfoOptional.get();
            IslandMessage islandMessage = getIslandMessage(islandInfo);
            responseBuilder
                    .setIsland(islandMessage)
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
     * @param request 可根据 name，hostId，subscriberId 查询
     * @param responseObserver
     */
    @Override
    public void retrieveIslandsByCondition(RetrieveMultipleIslandsRequest request, StreamObserver<IslandsResponse> responseObserver) {
        List<IslandMessage> islandMessageList = new ArrayList<>();
        Page<Long> islandListPageable = null;
        PageRequest pageRequest = request.getPageRequest();
        QueryIslandCondition requestCondition = request.getCondition();
        // 如果参数中有 name ，目前的版本是精确匹配，所以只返回一条记录
        if (requestCondition.hasName()) {
            IslandInfo islandInfo = islandInfoRepository.findByIslandNameAndDeletedIsFalse(requestCondition.getName().getValue());
            islandMessageList.add(getIslandMessage(islandInfo));
        } else { // 如果传入的是hostId，返回hostId创建的岛的列表；否则返回subscriberId加入的岛的列表
            int page = pageRequest.getPage();
            int pageSize = pageRequest.getPageSize();
            Pageable pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);

            if (requestCondition.hasHostId()) {
                islandListPageable = subscriptionService.getIslandIdListByUserCreated(Long.valueOf(requestCondition.getHostId().getValue()), pageable);
            } else {
                islandListPageable = subscriptionService.getIslandIdListByUserSubscribed(Long.valueOf(requestCondition.getSubscribedUserId().getValue()), pageable);
            }

            List<IslandInfo> islandInfoList = islandInfoRepository.findAllById(islandListPageable.getContent());
            islandMessageList = islandInfoList.stream().map(this::getIslandMessage).collect(Collectors.toList());
        }

        // 构建返回信息
        IslandsResponse.Builder builder = IslandsResponse.newBuilder()
                .addAllIslands(islandMessageList);
        // 如果是按 name 查询，islandListPageable为null，就不会有pageResponse
        if (islandListPageable != null) {
            PageResponse pageResponse = PageResponse.newBuilder()
                    .setPage(pageRequest.getPage())
                    .setPageSize(pageRequest.getPageSize())
                    .setHasContent(islandListPageable.hasContent())
                    .setHasMore(pageRequest.getPage() < islandListPageable.getTotalPages())
                    .build();
            builder.setPageResponse(pageResponse);
        }
        IslandsResponse islandsResponse = builder.setStatus(CommonStatusUtils.getSuccStatus()).build();
        responseObserver.onNext(islandsResponse);
        responseObserver.onCompleted();
    }

    /**
     * 通过 id 更新 island 的信息，部分更新，先查后改
     * @param request
     * @param responseObserver
     */
    @Override
    public void updateIslandById(UpdateIslandByIdRequest request, StreamObserver<IslandResponse> responseObserver) {
        Optional<IslandInfo> optionalIslandInfo = islandInfoRepository.findById(Long.valueOf(request.getId()));
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
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveIslandProfileById(RetrieveIslandProfileByIdRequest request, StreamObserver<IslandProfileResponse> responseObserver) {
        boolean islandFound = false;
        boolean userFound = false;
        IslandProfileResponse.Builder responseBuilder = IslandProfileResponse.newBuilder();
        Optional<IslandInfo> islandInfoOptional = islandInfoRepository.findById(Long.valueOf(request.getId()));
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
                // todo: 没有subscribed字段，httpserver加上还是让客户端自己去判断
                .build();
        responseObserver.onNext(islandProfileResponse);
        responseObserver.onCompleted();
    }

    /**
     * 根据 island id 返回岛的所有订阅者信息
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
        Page<Long> subscriberIdListPageable = subscriptionService.getSubscriberIdListByIslandId(Long.valueOf(islandId), pageable);
        List<Long> subscriberIdList = subscriberIdListPageable.getContent();
        //根据idList拿到UserInfoList并转化为UserMessageList
        List<UserMessage> userMessageList = userInfoService.getUserMessageListByIdList(subscriberIdList);

        PageResponse pageResponse = PageResponse.newBuilder()
                .setPage(page)
                .setPageSize(pageSize)
                .setHasMore(subscriberIdListPageable.getTotalPages() > page)
                .setHasContent(subscriberIdListPageable.hasContent())
                .build();
         IslandSubscribersResponse islandSubscribersResponse =
                 IslandSubscribersResponse.newBuilder()
                        .setPageResponse(pageResponse)
                        .addAllUser(userMessageList)
                        .setStatus(CommonStatusUtils.getSuccStatus())
                        .build();
        responseObserver.onNext(islandSubscribersResponse);
        responseObserver.onCompleted();
    }

    /**
     * 订阅一个岛
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
        Optional<IslandInfo> islandInfoOptional = islandInfoRepository.findById(Long.valueOf(islandId));
        if (islandInfoOptional.isPresent()) {
            IslandInfo islandInfo = islandInfoOptional.get();
            if (secret.equals(islandInfo.getSecret())) {
                Integer islanderNumber = islandInfoRepository.getIslanderNumberByIslandId(Long.valueOf(islandId));
                subscriptionService.subscribeIsland(Long.valueOf(islandId), Long.valueOf(userId), islanderNumber);
                islandInfoRepository.updateIslanderNumberById(Long.valueOf(islanderNumber));
            } else {
                CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_ISLAND_SECRET_ERROR);
                responseBuilder.setStatus(commonStatus);
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
     * @param request
     * @param responseObserver
     */
    @Override
    public void unsubscribeIslandById(UnsubscribeIslandByIdRequest request, StreamObserver<SubscribeIslandResponse> responseObserver) {
        String islandId = request.getId();
        String userId = request.getUserId();
        subscriptionService.unSubscribeIsland(Long.valueOf(islandId), Long.valueOf(userId));

        SubscribeIslandResponse subscribeIslandResponse = SubscribeIslandResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus()).build();
        responseObserver.onNext(subscribeIslandResponse);
        responseObserver.onCompleted();
    }

    /**
     * 根据islandId和对应的timestamp检查是否有新的未读feed
     * @param request
     * @param responseObserver
     */
    @Override
    public void checkNewFeeds(CheckNewFeedsRequest request, StreamObserver<CheckNewFeedsResponse> responseObserver) {
        List<Long> islandIdList = request.getIslandIdsList().stream().map(Long::valueOf).collect(Collectors.toList());
        List<Long> timestampsList = request.getTimestampsList();
        List<CheckNewFeedsMessage> messageList = new ArrayList<>();
        if (islandIdList.size() == timestampsList.size()) {
            Map<Long, Long> map = islandInfoRepository.findIslandIdAndLastFeedAtByIslandIdList(islandIdList);
            for (int i = 0; i < islandIdList.size(); i++) {
                Long islandId = islandIdList.get(i);
                CheckNewFeedsMessage feedMessage = getFeedMessage(islandId.toString(), map.get(islandId) > timestampsList.get(i));
                messageList.add(feedMessage);
            }
        } else {
            // todo add error status
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
     * @param request
     * @param responseObserver
     */
    @Override
    public void updateLastFeedAtById(UpdateLastFeedAtRequest request, StreamObserver<UpdateLastFeedAtResponse> responseObserver) {
        List<Long> islandIdList = request.getIslandIdsList().stream().map(Long::valueOf).collect(Collectors.toList());
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
     * @param islandId
     * @return
     */
    public Integer getLatestIslanderNumber(Long islandId) {
        return islandInfoRepository.getIslanderNumberByIslandId(islandId);
    }

    private boolean islandNameIsExisted(String islandName) {
        return islandInfoRepository.findByIslandNameAndDeletedIsFalse(islandName) != null;
    }

    /**
     * 把 IslandInfo 对象包装为 IslandMessage 对象
     * @param islandInfo
     * @return
     */
    private IslandMessage getIslandMessage(IslandInfo islandInfo) {
        Integer memberCount = subscriptionService.getMemberCountByIslandId(islandInfo.getId());
        return IslandMessage
                .newBuilder()
                .setId(islandInfo.getId().toString())
                .setName(islandInfo.getIslandName())
                .setHostId(islandInfo.getHostId().toString())
                .setPortraitImageUri(islandInfo.getPortraitImageUri())
                .setDescription(islandInfo.getDescription())
                .setLastFeedAt(islandInfo.getLastFeedAt())
                .setCreatedAt(islandInfo.getCreatedTime())
                .setSecret(islandInfo.getSecret())
                .setMemberCount(memberCount)
                .build();
    }

    private CheckNewFeedsMessage getFeedMessage(String islandId, boolean hasNewFeeds) {
        return CheckNewFeedsMessage.newBuilder()
                .setIslandId(islandId)
                .setHasNewFeeds(hasNewFeeds)
                .build();
    }
}
