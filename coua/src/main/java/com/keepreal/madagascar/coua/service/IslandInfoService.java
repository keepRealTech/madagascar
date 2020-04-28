package com.keepreal.madagascar.coua.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.PageRequest;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.coua.*;
import com.keepreal.madagascar.coua.dao.IslandInfoRepository;
import com.keepreal.madagascar.coua.model.IslandInfo;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/
@GRpcService
public class IslandInfoService extends IslandServiceGrpc.IslandServiceImplBase {

    @Autowired
    private IslandInfoRepository islandInfoRepository;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 检查island name 是否已经存在
     * @param request
     * @param responseObserver
     */
    @Override
    public void checkName(CheckNameRequest request, StreamObserver<CheckNameResponse> responseObserver) {
        String islandName = request.getName();
        boolean isExisted = islandNameIsExisted(islandName);
        CheckNameResponse checkNameResponse = CheckNameResponse.newBuilder().setIsExisted(isExisted).build();
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
        islandInfo.setPortraitImageUri(request.getPortraitImageUri().getValue());
        islandInfo.setSecret(request.getSecret().getValue());
        islandInfo.setLastFeedAt(System.currentTimeMillis());
        islandInfo.setCreateTime(System.currentTimeMillis());
        islandInfo.setUpdateTime(System.currentTimeMillis());
        // 将数据插入 island 表
        IslandInfo save = islandInfoRepository.save(islandInfo);
        // 维护 subscription 表
        String hostId = request.getHostId();
        subscriptionService.initHost(islandId, Long.valueOf(hostId));

        IslandMessage islandMessage = getIslandMessage(save);
        IslandResponse islandResponse = IslandResponse.newBuilder().setIsland(islandMessage).build();
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
        String islandId = request.getId();
        IslandInfo islandInfo = islandInfoRepository.findById(Long.valueOf(islandId))
                                            .orElseThrow(RuntimeException::new);

        IslandMessage islandMessage = getIslandMessage(islandInfo);
        IslandResponse islandResponse = IslandResponse.newBuilder().setIsland(islandMessage).build();
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
            IslandInfo islandInfo = islandInfoRepository.findByIslandName(requestCondition.getName().getValue());
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
            islandInfoList.forEach(info -> islandMessageList.add(getIslandMessage(info)));
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
        IslandsResponse islandsResponse = builder.build();
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
        IslandInfo islandInfo = optionalIslandInfo.orElseThrow(RuntimeException::new);// todo: 假设找不到按抛异常处理
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
        islandInfo.setUpdateTime(System.currentTimeMillis());
        IslandInfo save = islandInfoRepository.save(islandInfo);

        IslandMessage islandMessage = getIslandMessage(save);
        IslandResponse islandResponse = IslandResponse.newBuilder().setIsland(islandMessage).build();
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
        String islandId = request.getId();
        IslandInfo islandInfo = islandInfoRepository.findById(Long.valueOf(islandId))
                .orElseThrow(RuntimeException::new); // todo: 如果找不到默认抛异常

        IslandMessage islandMessage = getIslandMessage(islandInfo);
        UserMessage userMessage = userInfoService.getUserMessageById(islandInfo.getHostId());
        Integer userIndex = subscriptionService.getUserIndexByIslandId(islandInfo.getId(), islandInfo.getHostId());

        IslandProfileResponse islandProfileResponse = IslandProfileResponse.newBuilder()
                .setIsland(islandMessage)
                .setHost(userMessage)
                .setUserIndex(StringValue.newBuilder().setValue(userIndex.toString()).build())
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
    public void subscribeIslandById(SubscribeIslandByIdRequest request, StreamObserver<SubscribeIslandResponse> responseObserver) {
        String islandId = request.getId();
        String secret = request.getSecret();
        String userId = request.getUserId();

        IslandInfo islandInfo = islandInfoRepository.findById(Long.valueOf(islandId)).orElseThrow(RuntimeException::new);
        if (!secret.equals(islandInfo.getSecret())) {
            // throw exception?
        }
        //todo 这个方法里面的实现待商议
        subscriptionService.subscribeIsland(Long.valueOf(islandId), Long.valueOf(userId));

        SubscribeIslandResponse subscribeIslandResponse = SubscribeIslandResponse.newBuilder().build();
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
        subscriptionService.unSubscripeIsland(Long.valueOf(islandId), Long.valueOf(userId));

        SubscribeIslandResponse subscribeIslandResponse = SubscribeIslandResponse.newBuilder().build();
        responseObserver.onNext(subscribeIslandResponse);
        responseObserver.onCompleted();
    }

    private boolean islandNameIsExisted(String islandName) {
        return islandInfoRepository.findByIslandName(islandName) != null;
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
                .setCreatedAt(islandInfo.getCreateTime())
                .setSecret(islandInfo.getSecret())
                .setMemberCount(memberCount)
                .build();
    }
}
