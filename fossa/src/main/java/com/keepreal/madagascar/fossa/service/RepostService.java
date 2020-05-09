package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.RepostMessage;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.FeedRepostMessage;
import com.keepreal.madagascar.fossa.FeedRepostResponse;
import com.keepreal.madagascar.fossa.FeedRepostsResponse;
import com.keepreal.madagascar.fossa.IslandRepostMessage;
import com.keepreal.madagascar.fossa.IslandRepostResponse;
import com.keepreal.madagascar.fossa.IslandRepostsResponse;
import com.keepreal.madagascar.fossa.NewFeedRepostRequest;
import com.keepreal.madagascar.fossa.NewIslandRepostRequest;
import com.keepreal.madagascar.fossa.RepostServiceGrpc;
import com.keepreal.madagascar.fossa.RetrieveFeedRepostsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RetrieveIslandRepostsByIslandIdRequest;
import com.keepreal.madagascar.fossa.common.RepostType;
import com.keepreal.madagascar.fossa.dao.RepostRepository;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.model.RepostInfo;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
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
    private final FeedInfoService feedInfoService;

    @Autowired
    public RepostService(RepostRepository repostRepository, LongIdGenerator idGenerator, FeedInfoService feedInfoService) {
        this.repostRepository = repostRepository;
        this.idGenerator = idGenerator;
        this.feedInfoService = feedInfoService;
    }

    @Override
    public void createFeedRepost(NewFeedRepostRequest request, StreamObserver<FeedRepostResponse> responseObserver) {
        RepostInfo repostInfo = getRepostInfo(request.getFeedId(), request.getUserId(),
                request.getContent(), request.getIsSuccessful(), RepostType.FEED.getCode());
        RepostInfo save = repostRepository.save(repostInfo);

        feedInfoService.incFeedCount(request.getFeedId(), "repostCount");

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

        PageResponse pageResponse = PageRequestResponseUtils.buildPageResponse(repostInfoPageable);

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

        PageResponse pageResponse = PageRequestResponseUtils.buildPageResponse(repostInfoPageable);

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
