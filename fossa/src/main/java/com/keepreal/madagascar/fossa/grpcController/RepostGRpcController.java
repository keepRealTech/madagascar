package com.keepreal.madagascar.fossa.grpcController;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.DeviceType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.IslandResponse;
import com.keepreal.madagascar.fossa.FeedRepostMessage;
import com.keepreal.madagascar.fossa.FeedRepostResponse;
import com.keepreal.madagascar.fossa.FeedRepostsResponse;
import com.keepreal.madagascar.fossa.GenerateRepostCodeRequest;
import com.keepreal.madagascar.fossa.GenerateRepostCodeResponse;
import com.keepreal.madagascar.fossa.IslandRepostMessage;
import com.keepreal.madagascar.fossa.IslandRepostResponse;
import com.keepreal.madagascar.fossa.IslandRepostsResponse;
import com.keepreal.madagascar.fossa.NewFeedRepostRequest;
import com.keepreal.madagascar.fossa.NewIslandRepostRequest;
import com.keepreal.madagascar.fossa.RepostServiceGrpc;
import com.keepreal.madagascar.fossa.ResolveRepostCodeRequest;
import com.keepreal.madagascar.fossa.ResolveRepostCodeResponse;
import com.keepreal.madagascar.fossa.RetrieveFeedRepostsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RetrieveIslandRepostsByIslandIdRequest;
import com.keepreal.madagascar.fossa.common.FeedCountType;
import com.keepreal.madagascar.fossa.common.RepostType;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.service.IslandService;
import com.keepreal.madagascar.fossa.service.RepostService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.model.RepostInfo;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents repost GRpc Controller
 */
@GRpcService
public class RepostGRpcController extends RepostServiceGrpc.RepostServiceImplBase {

    private final RepostService repostService;
    private final FeedInfoService feedInfoService;
    private final IslandService islandService;

    /**
     * Constructs repost grpc controller.
     *
     * @param repostService   {@link RepostService}.
     * @param feedInfoService {@link FeedInfoService}.
     * @param islandService   {@link IslandService}.
     */
    public RepostGRpcController(RepostService repostService,
                                FeedInfoService feedInfoService,
                                IslandService islandService) {

        this.repostService = repostService;
        this.feedInfoService = feedInfoService;
        this.islandService = islandService;
    }

    /**
     * Create the feed repost.
     *
     * @param request          {@link NewFeedRepostRequest}.
     * @param responseObserver {@link FeedRepostResponse}.
     */
    @Override
    public void createFeedRepost(NewFeedRepostRequest request, StreamObserver<FeedRepostResponse> responseObserver) {
        RepostInfo repostInfo = this.repostService.save(request.getFeedId(), request.getUserId(),
                request.getContent(), request.getIsSuccessful(), RepostType.FEED.getValue());

        this.feedInfoService.incFeedCount(request.getFeedId(), FeedCountType.REPOST_COUNT);

        FeedRepostMessage message = this.repostService.getFeedRepostMessage(repostInfo);
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
     * @param request          {@link RetrieveFeedRepostsByFeedIdRequest}.
     * @param responseObserver {@link FeedRepostsResponse}.
     */
    @Override
    public void retrieveFeedRepostsByFeedId(RetrieveFeedRepostsByFeedIdRequest request, StreamObserver<FeedRepostsResponse> responseObserver) {
        String feedId = request.getFeedId();

        Page<RepostInfo> repostInfoPageable = repostService.getRepostInfoPageable(request.getPageRequest(), feedId, RepostType.FEED.getValue());
        List<FeedRepostMessage> repostMessageList = repostInfoPageable.getContent().stream().map(repostService::getFeedRepostMessage).filter(Objects::nonNull).collect(Collectors.toList());

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
     * @param request          {@link NewIslandRepostRequest}.
     * @param responseObserver {@link IslandRepostResponse}.
     */
    @Override
    public void createIslandRepost(NewIslandRepostRequest request, StreamObserver<IslandRepostResponse> responseObserver) {
        RepostInfo repostInfo = repostService.save(request.getIslandId(), request.getUserId(),
                request.getContent(), request.getIsSuccessful(), RepostType.ISLAND.getValue());

        IslandRepostMessage message = repostService.getIslandRepostMessage(repostInfo);
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
     * @param request          {@link RetrieveIslandRepostsByIslandIdRequest}.
     * @param responseObserver {@link IslandRepostsResponse}.
     */
    @Override
    public void retrieveIslandRepostsByIslandId(RetrieveIslandRepostsByIslandIdRequest request, StreamObserver<IslandRepostsResponse> responseObserver) {
        String islandId = request.getIslandId();

        Page<RepostInfo> repostInfoPageable = repostService.getRepostInfoPageable(request.getPageRequest(), islandId, RepostType.ISLAND.getValue());
        List<IslandRepostMessage> repostMessageList = repostInfoPageable.getContent().stream().map(repostService::getIslandRepostMessage).filter(Objects::nonNull).collect(Collectors.toList());

        PageResponse pageResponse = PageRequestResponseUtils.buildPageResponse(repostInfoPageable);

        IslandRepostsResponse response = IslandRepostsResponse.newBuilder()
                .addAllIslandReposts(repostMessageList)
                .setPageResponse(pageResponse)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void generateRepostCodeByIslandId(GenerateRepostCodeRequest request, StreamObserver<GenerateRepostCodeResponse> responseObserver) {
        String userId = request.getUserId();
        String islandId = request.getIslandId();
        IslandResponse response = islandService.retrieveIslandById(islandId);
        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            responseObserver.onNext(GenerateRepostCodeResponse.newBuilder()
                    .setStatus(response.getStatus())
                    .build());
            responseObserver.onCompleted();
            return;
        }
        boolean isHost = response.getIsland().getHostId().equals(userId);
        String encode = repostService.encode(islandId, isHost);
        String linkUrl = this.repostService.combineLinkUrl(response.getIsland().getId(), userId);
        String shortCode = this.repostService.generateShortCode(linkUrl);
        responseObserver.onNext(GenerateRepostCodeResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setCode(repostService.generatorCode(response.getIsland(), userId, encode, shortCode))
                .setLinkUrl(linkUrl)
                .setShortCode(shortCode)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void resolveRepostCode(ResolveRepostCodeRequest request, StreamObserver<ResolveRepostCodeResponse> responseObserver) {
        String code = request.getCode();
        DeviceType deviceType = request.getDeviceType();

        String islandId = repostService.decode(code);
        boolean withSecret = repostService.isHost(code);

        ResolveRepostCodeResponse.Builder builder = ResolveRepostCodeResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setIslandId(islandId)
                .setRedirectUrl(repostService.getRedirectUrlByDeviceType(deviceType));

        if (withSecret) {
            IslandMessage islandMessage = islandService.retrieveIslandById(islandId).getIsland();
            builder.setSecret(StringValue.of(islandMessage.getSecret()));
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
