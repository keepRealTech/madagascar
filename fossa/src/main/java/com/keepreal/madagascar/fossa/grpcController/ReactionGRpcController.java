package com.keepreal.madagascar.fossa.grpcController;

import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.NewReactionRequest;
import com.keepreal.madagascar.fossa.ReactionResponse;
import com.keepreal.madagascar.fossa.ReactionServiceGrpc;
import com.keepreal.madagascar.fossa.ReactionsResponse;
import com.keepreal.madagascar.fossa.RetrieveReactionsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RevokeReactionRequest;
import com.keepreal.madagascar.fossa.common.FeedCountType;
import com.keepreal.madagascar.fossa.config.NotificationEventProducerConfiguration;
import com.keepreal.madagascar.fossa.dao.ReactionRepository;
import com.keepreal.madagascar.fossa.model.ReactionInfo;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.service.NotificationEventProducerService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Retrieves the reaction GRpc controller
 */
@Slf4j
@GRpcService
public class ReactionGRpcController extends ReactionServiceGrpc.ReactionServiceImplBase {

    private final ReactionRepository reactionRepository;
    private final LongIdGenerator idGenerator;
    private final FeedInfoService feedInfoService;
    private final NotificationEventProducerConfiguration notificationEventProducerConfiguration;
    private final NotificationEventProducerService notificationEventProducerService;
    private final ProducerBean producerBean;

    /**
     * Construct the reaction grpc controller
     *
     * @param reactionRepository                     {@link ReactionRepository}.
     * @param idGenerator                            {@link LongIdGenerator}.
     * @param feedInfoService                        {@link FeedInfoService}.
     * @param notificationEventProducerConfiguration {@link NotificationEventProducerConfiguration}.
     * @param notificationEventProducerService       {@link NotificationEventProducerService}.
     * @param producerBean                           {@link ProducerBean}.
     */
    public ReactionGRpcController(ReactionRepository reactionRepository,
                                  LongIdGenerator idGenerator,
                                  FeedInfoService feedInfoService,
                                  NotificationEventProducerConfiguration notificationEventProducerConfiguration,
                                  NotificationEventProducerService notificationEventProducerService,
                                  ProducerBean producerBean) {
        this.reactionRepository = reactionRepository;
        this.idGenerator = idGenerator;
        this.feedInfoService = feedInfoService;
        this.notificationEventProducerConfiguration = notificationEventProducerConfiguration;
        this.notificationEventProducerService = notificationEventProducerService;
        this.producerBean = producerBean;
    }

    /**
     * Implements create reaction method.
     *
     * @param request          {@link NewReactionRequest}.
     * @param responseObserver {@link ReactionResponse}.
     */
    @Override
    public void createReaction(NewReactionRequest request, StreamObserver<ReactionResponse> responseObserver) {
        String id = String.valueOf(idGenerator.nextId());
        String userId = request.getUserId();
        String feedId = request.getFeedId();

        Set<Integer> reactionTypesSet = new HashSet<>(request.getReactionTypesValueList());
        boolean requestHasLikeType = reactionTypesSet.contains(ReactionType.REACTION_LIKE_VALUE);
        ReactionInfo reactionInfo = reactionRepository.findTopByFeedIdAndUserId(feedId, userId);
        if (reactionInfo == null) {
            reactionInfo = ReactionInfo.builder()
                    .id(id)
                    .userId(userId)
                    .feedId(feedId)
                    .createdTime(System.currentTimeMillis())
                    .reactionTypeList(reactionTypesSet)
                    .build();
            if (requestHasLikeType) {
                feedInfoService.incFeedCount(feedId, FeedCountType.LIKES_COUNT);
            }
        } else {
            Set<Integer> integerSet = reactionInfo.getReactionTypeList();
            if (!integerSet.contains(ReactionType.REACTION_LIKE_VALUE) && requestHasLikeType) {
                feedInfoService.incFeedCount(feedId, FeedCountType.LIKES_COUNT);
            }
            integerSet.addAll(reactionTypesSet);
        }
        reactionRepository.save(reactionInfo);

        ReactionMessage reactionMessage = getReactionMessage(reactionInfo.getId(), feedId, userId, request.getReactionTypesList());
        FeedMessage feedMessage = feedInfoService.getFeedMessageById(feedId, userId);

        this.notificationEventProducerService.produceNewReactionsNotificationEventAsync(reactionMessage, feedMessage);

        basicReactionResponse(responseObserver, reactionMessage);
    }

    /**
     * Implements revoke reaction method.
     *
     * @param request          {@link RevokeReactionRequest}.
     * @param responseObserver {@link ReactionResponse}.
     */
    @Override
    public void revokeReaction(RevokeReactionRequest request, StreamObserver<ReactionResponse> responseObserver) {
        String feedId = request.getFeedId();
        String userId = request.getUserId();
        List<Integer> typesValueList = request.getReactionTypesValueList();

        ReactionInfo reactionInfo = reactionRepository.findTopByFeedIdAndUserId(feedId, userId);
        if (reactionInfo != null) {
            Set<Integer> oldReactionSet = new HashSet<>(reactionInfo.getReactionTypeList());
            reactionInfo.getReactionTypeList().removeAll(typesValueList);
            reactionRepository.save(reactionInfo);
            if (typesValueList.contains(ReactionType.REACTION_LIKE_VALUE) && oldReactionSet.contains(ReactionType.REACTION_LIKE_VALUE)) {
                feedInfoService.subFeedCount(reactionInfo.getFeedId(), FeedCountType.LIKES_COUNT);
            }
            ReactionMessage reactionMessage = getReactionMessage(reactionInfo.getId(), feedId, userId, request.getReactionTypesList());
            basicReactionResponse(responseObserver, reactionMessage);
            return;
        }
        ReactionResponse reactionResponse = ReactionResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(reactionResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements retrieves reactions by feed id method.
     *
     * @param request          {@link RetrieveReactionsByFeedIdRequest}.
     * @param responseObserver {@link ReactionsResponse}.
     */
    @Override
    public void retrieveReactionsByFeedId(RetrieveReactionsByFeedIdRequest request, StreamObserver<ReactionsResponse> responseObserver) {
        String feedId = request.getFeedId();

        Pageable pageable = PageRequestResponseUtils.getPageableByRequest(request.getPageRequest());
        Page<ReactionInfo> reactionInfoListPageable = reactionRepository.findReactionInfosByFeedId(feedId, pageable);
        List<ReactionInfo> reactionInfoList = reactionInfoListPageable.getContent();
        List<ReactionMessage> reactionMessageList = reactionInfoList.stream()
                .map(info -> getReactionMessage(
                        info.getId(),
                        info.getFeedId(),
                        info.getUserId(),
                        info.getReactionTypeList().stream()
                                .map(ReactionType::forNumber)
                                .collect(Collectors.toList())
                        )
                ).collect(Collectors.toList());

        PageResponse pageResponse = PageRequestResponseUtils.buildPageResponse(reactionInfoListPageable);
        ReactionsResponse reactionsResponse = ReactionsResponse.newBuilder()
                .addAllReactions(reactionMessageList)
                .setPageResponse(pageResponse)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(reactionsResponse);
        responseObserver.onCompleted();
    }

    /**
     * Retrieves reaction message
     *
     * @param id       reaction id.
     * @param feedId   feed id.
     * @param userId   user id.
     * @param typeList reaction type list.
     * @return {@link ReactionMessage}.
     */
    private ReactionMessage getReactionMessage(String id, String feedId, String userId, List<ReactionType> typeList) {
        return ReactionMessage.newBuilder()
                .setId(id)
                .setFeedId(feedId)
                .setUserId(userId)
                .addAllReactionType(typeList)
                .setCreatedAt(System.currentTimeMillis())
                .build();
    }

    /**
     * Reaction response
     *
     * @param responseObserver {@link ReactionsResponse}.
     * @param reactionMessage  {@link ReactionMessage}.
     */
    private void basicReactionResponse(StreamObserver<ReactionResponse> responseObserver, ReactionMessage reactionMessage) {
        ReactionResponse reactionResponse = ReactionResponse.newBuilder()
                .setReaction(reactionMessage)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(reactionResponse);
        responseObserver.onCompleted();
    }
}
