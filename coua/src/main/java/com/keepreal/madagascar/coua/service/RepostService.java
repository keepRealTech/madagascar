package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.RepostMessage;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.FeedRepostMessage;
import com.keepreal.madagascar.coua.FeedRepostResponse;
import com.keepreal.madagascar.coua.FeedRepostsResponse;
import com.keepreal.madagascar.coua.IslandRepostMessage;
import com.keepreal.madagascar.coua.IslandRepostResponse;
import com.keepreal.madagascar.coua.IslandRepostsResponse;
import com.keepreal.madagascar.coua.NewFeedRepostRequest;
import com.keepreal.madagascar.coua.NewIslandRepostRequest;
import com.keepreal.madagascar.coua.RepostServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveFeedRepostsByFeedIdRequest;
import com.keepreal.madagascar.coua.RetrieveIslandRepostsByIslandIdRequest;
import com.keepreal.madagascar.coua.common.RepostType;
import com.keepreal.madagascar.coua.dao.RepostRepository;
import com.keepreal.madagascar.coua.model.RepostInfo;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import com.keepreal.madagascar.coua.util.PageResponseUtil;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-06
 **/

@GRpcService
public class RepostService extends RepostServiceGrpc.RepostServiceImplBase {

    private final RepostRepository repostRepository;
    private final LongIdGenerator idGenerator;

    @Autowired
    public RepostService(RepostRepository repostRepository, LongIdGenerator idGenerator) {
        this.repostRepository = repostRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public void createFeedRepost(NewFeedRepostRequest request, StreamObserver<FeedRepostResponse> responseObserver) {
        RepostInfo repostInfo = getRepostInfo(request.getFeedId(), request.getUserId(),
                request.getContent(), request.getIsSuccessful(), RepostType.FEED.getCode());
        RepostInfo save = repostRepository.save(repostInfo);

        FeedRepostMessage message = getFeedRepostMessage(save);
        FeedRepostResponse response = FeedRepostResponse.newBuilder()
                .setFeedRepost(message)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveFeedRepostsByFeedId(RetrieveFeedRepostsByFeedIdRequest request, StreamObserver<FeedRepostsResponse> responseObserver) {
        String feedId = request.getFeedId();

        Page<RepostInfo> repostInfoPageable = getRepostInfoPageable(request.getPageRequest(), feedId);
        List<FeedRepostMessage> repostMessageList = repostInfoPageable.getContent().stream().map(this::getFeedRepostMessage).filter(Objects::nonNull).collect(Collectors.toList());

        PageResponse pageResponse = PageResponseUtil.buildResponse(repostInfoPageable);

        FeedRepostsResponse response = FeedRepostsResponse.newBuilder()
                .addAllFeedReposts(repostMessageList)
                .setPageResponse(pageResponse)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createIslandRepost(NewIslandRepostRequest request, StreamObserver<IslandRepostResponse> responseObserver) {
        RepostInfo repostInfo = getRepostInfo(request.getIslandId(), request.getUserId(),
                request.getContent(), request.getIsSuccessful(), RepostType.FEED.getCode());
        RepostInfo save = repostRepository.save(repostInfo);

        IslandRepostMessage message = getIslandRepostMessage(save);
        IslandRepostResponse response = IslandRepostResponse.newBuilder()
                .setIslandRepost(message)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveIslandRepostsByIslandId(RetrieveIslandRepostsByIslandIdRequest request, StreamObserver<IslandRepostsResponse> responseObserver) {
        String islandId = request.getIslandId();

        Page<RepostInfo> repostInfoPageable = getRepostInfoPageable(request.getPageRequest(), islandId);
        List<IslandRepostMessage> repostMessageList = repostInfoPageable.getContent().stream().map(this::getIslandRepostMessage).filter(Objects::nonNull).collect(Collectors.toList());

        PageResponse pageResponse = PageResponseUtil.buildResponse(repostInfoPageable);

        IslandRepostsResponse response = IslandRepostsResponse.newBuilder()
                .addAllIslandReposts(repostMessageList)
                .setPageResponse(pageResponse)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Page<RepostInfo> getRepostInfoPageable(com.keepreal.madagascar.common.PageRequest pageRequest, String fromId) {
        return repostRepository.findRepostInfosByFromId(fromId, PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize()));
    }

    private RepostInfo getRepostInfo(String fromId, String userId, String content, Boolean isSuccessful, Integer fromType) {
        return RepostInfo.builder()
                .id(String.valueOf(idGenerator.nextId()))
                .fromId(fromId)
                .userId(userId)
                .content(content)
                .successful(isSuccessful)
                .fromType(fromType)
                .build();
    }

    private RepostMessage getRepostMessage(RepostInfo repostInfo) {
        return RepostMessage.newBuilder()
                .setContent(repostInfo.getContent())
                .setCreatedAt(repostInfo.getCreatedTime())
                .setId(repostInfo.getId())
                .setIsSuccessful(repostInfo.getSuccessful())
                .setUserId(repostInfo.getUserId())
                .build();
    }

    private IslandRepostMessage getIslandRepostMessage(RepostInfo repostInfo) {
        if (repostInfo == null) {
            return null;
        }
        RepostMessage repostMessage = getRepostMessage(repostInfo);
        return IslandRepostMessage.newBuilder()
                .setIslandId(repostInfo.getFromId())
                .setIslandRepost(repostMessage)
                .build();
    }

    private FeedRepostMessage getFeedRepostMessage(RepostInfo repostInfo) {
        if (repostInfo == null) {
            return null;
        }
        RepostMessage repostMessage = getRepostMessage(repostInfo);
        return FeedRepostMessage.newBuilder()
                .setFeedId(repostInfo.getFromId())
                .setFeedRepost(repostMessage)
                .build();
    }
}
