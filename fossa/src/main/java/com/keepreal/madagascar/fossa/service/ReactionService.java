package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.fossa.NewReactionRequest;
import com.keepreal.madagascar.fossa.ReactionResponse;
import com.keepreal.madagascar.fossa.ReactionServiceGrpc;
import com.keepreal.madagascar.fossa.ReactionsResponse;
import com.keepreal.madagascar.fossa.RetrieveReactionsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RevokeReactionRequest;
import com.keepreal.madagascar.fossa.dao.ReactionRepository;
import com.keepreal.madagascar.fossa.model.ReactionInfo;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
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

    @Autowired
    public ReactionService(ReactionRepository reactionRepository) {
        this.reactionRepository = reactionRepository;
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
        long currentTimeMillis = System.currentTimeMillis();
        if (request.getReactionTypesCount() > 0) {
            List<Integer> typesValueList = request.getReactionTypesValueList();
            typesValueList.forEach(type -> {
                ReactionInfo reactionInfo = new ReactionInfo();
                reactionInfo.setUserId(Long.valueOf(userId));
                reactionInfo.setFeedId(Long.valueOf(feedId));
                reactionInfo.setReactionType(type);
                reactionInfo.setDeleted(false);
                reactionInfo.setCreatedTime(currentTimeMillis);
                reactionInfo.setUpdatedTime(currentTimeMillis);
                reactionRepository.save(reactionInfo);
            });
        }

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
        List<ReactionInfo> reactionInfoList = reactionRepository
                .findReactionInfosByFeedIdAndUserIdAndReactionTypeIn(Long.valueOf(feedId), Long.valueOf(userId), typesValueList);
        reactionInfoList.forEach(r -> r.setDeleted(true));
        reactionRepository.saveAll(reactionInfoList);

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
        List<ReactionMessage> reactionMessageList = reactionInfoList.stream()
                .map(info -> getReactionMessage(info.getFeedId().toString(), info.getUserId().toString(), ReactionType.forNumber(info.getReactionType())))
                .collect(Collectors.toList());

        PageResponse pageResponse = PageResponse.newBuilder()
                .setPage(pageable.getPageNumber())
                .setPageSize(pageable.getPageSize())
                .setHasContent(reactionInfoListPageable.hasContent())
                .setHasMore(reactionInfoListPageable.getTotalPages() > pageable.getPageNumber())
                .build();
        ReactionsResponse reactionsResponse = ReactionsResponse.newBuilder()
                .addAllReactions(reactionMessageList)
                .setPageResponse(pageResponse)
                .build();
        responseObserver.onNext(reactionsResponse);
        responseObserver.onCompleted();
    }

    private ReactionMessage getReactionMessage(String feedId, String userId, ReactionType type) {
        return getReactionMessage(feedId, userId, Collections.singletonList(type));
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
        ReactionResponse reactionResponse = ReactionResponse
                .newBuilder().setReaction(reactionMessage).build();
        responseObserver.onNext(reactionResponse);
        responseObserver.onCompleted();
    }
}
