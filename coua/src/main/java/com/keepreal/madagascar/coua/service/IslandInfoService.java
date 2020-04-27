package com.keepreal.madagascar.coua.service;

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
import java.util.stream.Collectors;

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

    @Override
    public void checkName(CheckNameRequest request, StreamObserver<CheckNameResponse> responseObserver) {
        String islandName = request.getName();
        boolean isExisted = islandNameIsExisted(islandName);
        CheckNameResponse checkNameResponse = CheckNameResponse.newBuilder().setIsExisted(isExisted).build();
        responseObserver.onNext(checkNameResponse);
        responseObserver.onCompleted();
    }

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

        IslandInfo save = islandInfoRepository.save(islandInfo);

        String hostId = request.getHostId();
        subscriptionService.initHost(islandId, Long.valueOf(hostId));

        IslandMessage islandMessage = getIslandMessage(save);
        IslandResponse islandResponse = IslandResponse.newBuilder().setMessage(islandMessage).build();
        responseObserver.onNext(islandResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveIslandById(RetrieveIslandByIdRequest request, StreamObserver<IslandResponse> responseObserver) {
        String islandId = request.getId();
        IslandInfo islandInfo = islandInfoRepository.findById(Long.valueOf(islandId))
                                            .orElseThrow(RuntimeException::new);

        IslandMessage islandMessage = getIslandMessage(islandInfo);
        IslandResponse islandResponse = IslandResponse.newBuilder().setMessage(islandMessage).build();
        responseObserver.onNext(islandResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveIslandsByCondition(RetrieveMultipleIslandsRequest request, StreamObserver<IslandsResponse> responseObserver) {
        List<IslandMessage> islandMessageList = new ArrayList<>();
        Page<Long> islandListPageable = null;
        PageRequest pageRequest = request.getPageRequest();
        QueryIslandCondition requestCondition = request.getCondition();
        if (requestCondition.hasName()) {
            IslandInfo islandInfo = islandInfoRepository.findByIslandName(requestCondition.getName().getValue());
            islandMessageList.add(getIslandMessage(islandInfo));
        } else {
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

        IslandsResponse.Builder builder = IslandsResponse.newBuilder()
                .addAllMessage(islandMessageList);
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
        IslandResponse islandResponse = IslandResponse.newBuilder().setMessage(islandMessage).build();
        responseObserver.onNext(islandResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveIslandProfileById(RetrieveIslandProfileByIdRequest request, StreamObserver<IslandProfileResponse> responseObserver) {
        String islandId = request.getId();
        IslandInfo islandInfo = islandInfoRepository.findById(Long.valueOf(islandId))
                .orElseThrow(RuntimeException::new);
        IslandMessage islandMessage = getIslandMessage(islandInfo);

        UserMessage userMessage = userInfoService.getUserMessageById(islandInfo.getHostId());

        IslandProfileResponse islandProfileResponse = IslandProfileResponse.newBuilder()
                .setIslandMessage(islandMessage)
                .setUserMessage(userMessage)
                .build();
        responseObserver.onNext(islandProfileResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveIslandSubscribersById(RetrieveIslandSubscribersByIdRequest request, StreamObserver<RetrieveIslandSubscribersByIdResponse> responseObserver) {
        String islandId = request.getId();
        int page = request.getPageRequest().getPage();
        int pageSize = request.getPageRequest().getPageSize();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, pageSize);
        Page<Long> subscriberIdListPageable = subscriptionService.getSubscriberIdListByIslandId(Long.valueOf(islandId), pageable);
        List<Long> subscriberIdList = subscriberIdListPageable.getContent();
        List<UserMessage> userMessageList = subscriberIdList.stream().map(id -> userInfoService.getUserMessageById(id)).collect(Collectors.toList());

        PageResponse pageResponse = PageResponse.newBuilder()
                .setPage(page)
                .setPageSize(pageSize)
                .setHasMore(subscriberIdListPageable.getTotalPages() > page)
                .setHasContent(subscriberIdListPageable.hasContent())
                .build();
        RetrieveIslandSubscribersByIdResponse retrieveIslandSubscribersByIdResponse =
                RetrieveIslandSubscribersByIdResponse.newBuilder()
                        .setPageResponse(pageResponse)
                        .addAllUserMessage(userMessageList)
                        .build();
        responseObserver.onNext(retrieveIslandSubscribersByIdResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void subscribeIslandById(SubscribeIslandByIdRequest request, StreamObserver<SubscribeIslandResponse> responseObserver) {
        String islandId = request.getId();
        String secret = request.getSecret();
        String userId = request.getUserId();

        IslandInfo islandInfo = islandInfoRepository.findById(Long.valueOf(islandId)).orElseThrow(RuntimeException::new);
        if (!secret.equals(islandInfo.getSecret())) {
            // throw exception?
        }
        //todo
        subscriptionService.subscribeIsland(Long.valueOf(islandId), Long.valueOf(userId));

        SubscribeIslandResponse subscribeIslandResponse = SubscribeIslandResponse.newBuilder().build();
        responseObserver.onNext(subscribeIslandResponse);
        responseObserver.onCompleted();
    }

    private boolean islandNameIsExisted(String islandName) {
        return islandInfoRepository.findByIslandName(islandName) != null;
    }

    private IslandMessage getIslandMessage(IslandInfo islandInfo) {
        return IslandMessage
                .newBuilder()
                .setId(islandInfo.getId().toString())
                .setName(islandInfo.getIslandName())
                .setHostId(islandInfo.getHostId().toString())
                .setPortraitImageUri(islandInfo.getPortraitImageUri())
                .setDescription(islandInfo.getDescription())
                .setLastFeedAt(islandInfo.getLastFeedAt())
                .setCreatedAt(islandInfo.getCreateTime())
                .build();
    }
}
