package com.keepreal.madagascar.fossa.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.NewReactionRequest;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import com.keepreal.madagascar.tenrecs.ReactionEvent;
import com.keepreal.madagascar.fossa.ReactionResponse;
import com.keepreal.madagascar.fossa.ReactionServiceGrpc;
import com.keepreal.madagascar.fossa.ReactionsResponse;
import com.keepreal.madagascar.fossa.RetrieveReactionsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RevokeReactionRequest;
import com.keepreal.madagascar.fossa.config.MqConfig;
import com.keepreal.madagascar.fossa.dao.ReactionRepository;
import com.keepreal.madagascar.fossa.model.ReactionInfo;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import com.keepreal.madagascar.fossa.util.ProducerUtils;
import com.mongodb.client.result.UpdateResult;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Slf4j
@GRpcService
public class ReactionService extends ReactionServiceGrpc.ReactionServiceImplBase {

    private final ReactionRepository reactionRepository;
    private final LongIdGenerator idGenerator;
    private final MongoTemplate mongoTemplate;
    private final FeedInfoService feedInfoService;
    private final MqConfig mqConfig;
    private final ProducerBean producerBean;

    @Autowired
    public ReactionService(ReactionRepository reactionRepository, LongIdGenerator idGenerator, MongoTemplate mongoTemplate, FeedInfoService feedInfoService, MqConfig mqConfig, ProducerBean producerBean) {
        this.reactionRepository = reactionRepository;
        this.idGenerator = idGenerator;
        this.mongoTemplate = mongoTemplate;
        this.feedInfoService = feedInfoService;
        this.mqConfig = mqConfig;
        this.producerBean = producerBean;
    }

    /**
     * 创建reaction
     * todo 创建时检测是否已经存在
     * @param request
     * @param responseObserver
     */
    @Override
    public void createReaction(NewReactionRequest request, StreamObserver<ReactionResponse> responseObserver) {
        String id = String.valueOf(idGenerator.nextId());
        String userId = request.getUserId();
        String feedId = request.getFeedId();

        Set<Integer> reactionTypesSet = new HashSet<>(request.getReactionTypesValueList());
        boolean requesthasLikeType = reactionTypesSet.contains(ReactionType.REACTION_LIKE_VALUE);
        ReactionInfo reactionInfo = reactionRepository.findTopByFeedIdAndUserId(feedId, userId);
        if (reactionInfo == null) {
            reactionInfo = new ReactionInfo();
            reactionInfo.setId(id);
            reactionInfo.setUserId(request.getUserId());
            reactionInfo.setUpdatedTime(Long.valueOf(userId));
            reactionInfo.setFeedId(feedId);
            reactionInfo.setReactionTypeList(reactionTypesSet);
            reactionInfo.setDeleted(false);
            if (requesthasLikeType) {
                feedInfoService.incFeedCount(feedId, "likesCount");
            }
        } else {
            Set<Integer> integerSet = reactionInfo.getReactionTypeList();
            if (!integerSet.contains(ReactionType.REACTION_LIKE_VALUE) && requesthasLikeType) {
                feedInfoService.incFeedCount(feedId, "likesCount");
            }
            integerSet.addAll(reactionTypesSet);
        }
        reactionRepository.save(reactionInfo);

        ReactionMessage reactionMessage = getReactionMessage(id ,feedId, userId, request.getReactionTypesList());
        FeedMessage feedMessage = feedInfoService.getFeedMessageById(feedId);
        ReactionEvent reactionEvent = ReactionEvent.newBuilder()
                .setReaction(reactionMessage)
                .setFeed(feedMessage)
                .build();
        String uuid = UUID.randomUUID().toString();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_REACTION)
                .setUserId(feedMessage.getUserId())
                .setReactionEvent(reactionEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        Message message = new Message(mqConfig.getTopic(), mqConfig.getTag(), event.toByteArray());
        message.setKey(uuid);
        ProducerUtils.sendMessageAsync(producerBean, message);
        basicResponse(responseObserver, reactionMessage);
    }

    /**
     * 取消reaction
     * @param request
     * @param responseObserver
     */
    @Override
    public void revokeReaction(RevokeReactionRequest request, StreamObserver<ReactionResponse> responseObserver) {
        String feedId = request.getFeedId();
        String userId = request.getUserId();
        List<Integer> typesValueList = request.getReactionTypesValueList();

        Query query = Query.query(Criteria.where("feedId").is(feedId).and("userId").is(userId));
        Update update = new Update();
        update.pullAll("reactionTypeList", typesValueList.toArray());
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, ReactionInfo.class);
        BsonValue upsertedId = updateResult.getUpsertedId();
        if (upsertedId != null) {
            ReactionMessage reactionMessage = getReactionMessage(upsertedId.asString().toString(), feedId, userId, request.getReactionTypesList());
            basicResponse(responseObserver, reactionMessage);
        }

    }

    /**
     * 根据feedId返回当前feed的所有reaction信息
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveReactionsByFeedId(RetrieveReactionsByFeedIdRequest request, StreamObserver<ReactionsResponse> responseObserver) {
        String feedId = request.getFeedId();

        Pageable pageable = PageRequestResponseUtils.getPageableByRequest(request.getPageRequest());
        Page<ReactionInfo> reactionInfoListPageable = reactionRepository.findReactionInfosByFeedId(feedId, pageable);
        List<ReactionInfo> reactionInfoList = reactionInfoListPageable.getContent();
        List<ReactionMessage> reactionMessageList = reactionInfoList.stream() //这种写法是不是会让人看得很乱？
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

    private ReactionMessage getReactionMessage(String id, String feedId, String userId, List<ReactionType> typeList) {
        return ReactionMessage.newBuilder()
                .setId(id)
                .setFeedId(feedId)
                .setUserId(userId)
                .addAllReactionType(typeList)
                .setCreatedAt(System.currentTimeMillis())
                .build();
    }

    private void basicResponse(StreamObserver<ReactionResponse> responseObserver, ReactionMessage reactionMessage) {
        ReactionResponse reactionResponse = ReactionResponse.newBuilder()
                .setReaction(reactionMessage)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(reactionResponse);
        responseObserver.onCompleted();
    }
}
