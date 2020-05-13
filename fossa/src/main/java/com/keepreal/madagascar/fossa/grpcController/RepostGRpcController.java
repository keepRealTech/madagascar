package com.keepreal.madagascar.fossa.grpcController;

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
import com.keepreal.madagascar.fossa.common.FeedCountType;
import com.keepreal.madagascar.fossa.common.RepostType;
import com.keepreal.madagascar.fossa.dao.RepostRepository;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
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
 * Represents repost GRpc Controller
 */
@GRpcService
public class RepostGRpcController extends RepostServiceGrpc.RepostServiceImplBase {

    private final RepostRepository repostRepository;
    private final LongIdGenerator idGenerator;
    private final FeedInfoService feedInfoService;

    /**
     * Constructs repost grpc controller.
     *
     * @param repostRepository  {@link RepostRepository}.
     * @param idGenerator       {@link LongIdGenerator}.
     * @param feedInfoService   {@link FeedInfoService}.
     */
    public RepostGRpcController(RepostRepository repostRepository,
                                LongIdGenerator idGenerator,
                                FeedInfoService feedInfoService) {
        this.repostRepository = repostRepository;
        this.idGenerator = idGenerator;
        this.feedInfoService = feedInfoService;
    }

    /**
     * Create the feed repost.
     *
     * @param request           {@link NewFeedRepostRequest}.
     * @param responseObserver  {@link FeedRepostResponse}.
     */
    @Override
    public void createFeedRepost(NewFeedRepostRequest request, StreamObserver<FeedRepostResponse> responseObserver) {
        RepostInfo repostInfo = getRepostInfo(request.getFeedId(), request.getUserId(),
                request.getContent(), request.getIsSuccessful(), RepostType.FEED.getCode());
        RepostInfo save = repostRepository.save(repostInfo);

        feedInfoService.incFeedCount(request.getFeedId(), FeedCountType.REPOST_COUNT);

        FeedRepostMessage message = getFeedRepostMessage(save);
        FeedRepostResponse response = FeedRepostResponse.newBuilder()
                .setFeedRepost(message)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements feed reposts by feed id method.
     *
     * @param request           {@link RetrieveFeedRepostsByFeedIdRequest}.
     * @param responseObserver  {@link FeedRepostsResponse}.
     */
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

    /**
     * Create the island repost.
     *
     * @param request           {@link NewIslandRepostRequest}.
     * @param responseObserver  {@link IslandRepostResponse}.
     */
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

    /**
     * Implements island reposts by island id method.
     *
     * @param request           {@link RetrieveIslandRepostsByIslandIdRequest}.
     * @param responseObserver  {@link IslandRepostsResponse}.
     */
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

    /**
     * Retrieves pageable repost.
     *
     * @param pageRequest   {@link org.springframework.data.domain.Pageable}.
     * @param fromId        feed id or island id.
     * @return  {@link RepostInfo}.
     */
    private Page<RepostInfo> getRepostInfoPageable(com.keepreal.madagascar.common.PageRequest pageRequest, String fromId) {
        return repostRepository.findRepostInfosByFromId(fromId, PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize()));
    }

    /**
     * Retrieves repost
     *
     * @param fromId        feed id or island id.
     * @param userId        user id.
     * @param content       repost content.
     * @param isSuccessful  is successful.
     * @param fromType      repost type (feed or island)
     * @return
     */
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

    /**
     * Retrieves repost message.
     *
     * @param repostInfo    {@link RepostInfo}.
     * @return  {@link RepostMessage}.
     */
    private RepostMessage getRepostMessage(RepostInfo repostInfo) {
        return RepostMessage.newBuilder()
                .setContent(repostInfo.getContent())
                .setCreatedAt(repostInfo.getCreatedTime())
                .setId(repostInfo.getId())
                .setIsSuccessful(repostInfo.getSuccessful())
                .setUserId(repostInfo.getUserId())
                .build();
    }

    /**
     * Retrieves island repost message.
     *
     * @param repostInfo    {@link RepostInfo}.
     * @return  {@link IslandRepostMessage}.
     */
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

    /**
     * Retrieves feed repost message.
     *
     * @param repostInfo    {@link RepostInfo}.
     * @return  {@link FeedRepostMessage}.
     */
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
