package com.keepreal.madagascar.fossa.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.NewReactionRequest;
import com.keepreal.madagascar.fossa.NotificationEvent;
import com.keepreal.madagascar.fossa.NotificationEventType;
import com.keepreal.madagascar.fossa.ReactionEvent;
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
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
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
     * @param request
     * @param responseObserver
     */
    @Override
    public void createReaction(NewReactionRequest request, StreamObserver<ReactionResponse> responseObserver) {
        String userId = request.getUserId();
        String feedId = request.getFeedId();
        List<Integer> reactionTypesList = request.getReactionTypesValueList();
        ReactionInfo reactionInfo = new ReactionInfo();
        reactionInfo.setId(idGenerator.nextId());
        reactionInfo.setUpdatedTime(Long.valueOf(userId));
        reactionInfo.setFeedId(Long.valueOf(feedId));
        reactionInfo.setReactionTypeList(reactionTypesList);
        reactionInfo.setDeleted(false);
        reactionRepository.save(reactionInfo);

        ReactionMessage reactionMessage = getReactionMessage(feedId, userId, request.getReactionTypesList());

        ReactionEvent reactionEvent = ReactionEvent.newBuilder()
                .setReaction(reactionMessage)
                .setFeed(feedInfoService.getFeedMessageById(Long.valueOf(feedId)))
                .build();
        String uuid = UUID.randomUUID().toString();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_REACTION)
                .setUserId(userId)
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
        mongoTemplate.updateFirst(query, update, ReactionInfo.class);

        ReactionMessage reactionMessage = getReactionMessage(feedId, userId, request.getReactionTypesList());
        basicResponse(responseObserver, reactionMessage);
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
        Page<ReactionInfo> reactionInfoListPageable = reactionRepository.findReactionInfosByFeedId(Long.valueOf(feedId), pageable);
        List<ReactionInfo> reactionInfoList = reactionInfoListPageable.getContent();
        List<ReactionMessage> reactionMessageList = reactionInfoList.stream() //这种写法是不是会让人看得很乱？
                .map(info -> getReactionMessage(
                                info.getFeedId().toString(),
                                info.getUserId().toString(),
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

    private ReactionMessage getReactionMessage(String feedId, String userId, List<ReactionType> typeList) {
        return ReactionMessage.newBuilder()
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
