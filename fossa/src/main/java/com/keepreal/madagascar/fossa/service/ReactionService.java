package com.keepreal.madagascar.fossa.service;

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
import com.keepreal.madagascar.fossa.dao.ReactionRepository;
import com.keepreal.madagascar.fossa.model.ReactionInfo;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@GRpcService
public class ReactionService extends ReactionServiceGrpc.ReactionServiceImplBase {

    private final ReactionRepository reactionRepository;
    private final LongIdGenerator idGenerator;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ReactionService(ReactionRepository reactionRepository, LongIdGenerator idGenerator, MongoTemplate mongoTemplate) {
        this.reactionRepository = reactionRepository;
        this.idGenerator = idGenerator;
        this.mongoTemplate = mongoTemplate;
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
