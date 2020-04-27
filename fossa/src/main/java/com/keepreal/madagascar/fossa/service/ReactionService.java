package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.fossa.NewReactionRequest;
import com.keepreal.madagascar.fossa.ReactionResponse;
import com.keepreal.madagascar.fossa.ReactionServiceGrpc;
import com.keepreal.madagascar.fossa.ReactionsResponse;
import com.keepreal.madagascar.fossa.RetrieveReactionsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RevokeReactionRequest;
import com.keepreal.madagascar.fossa.model.ReactionInfo;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@GRpcService
public class ReactionService extends ReactionServiceGrpc.ReactionServiceImplBase {

    @Override
    public void createReaction(NewReactionRequest request, StreamObserver<ReactionResponse> responseObserver) {
        ReactionInfo reactionInfo = new ReactionInfo();
        reactionInfo.setUserId(Long.valueOf(request.getUserId()));
        reactionInfo.setFeedId(Long.valueOf(request.getFeedId()));
        List<ReactionType> reactionTypesList = request.getReactionTypesList();
        reactionInfo.setReactionType(request.getReactionTypesCount());
        super.createReaction(request, responseObserver);
    }

    @Override
    public void revokeReaction(RevokeReactionRequest request, StreamObserver<ReactionResponse> responseObserver) {
        super.revokeReaction(request, responseObserver);
    }

    @Override
    public void retrieveReactionsByFeedId(RetrieveReactionsByFeedIdRequest request, StreamObserver<ReactionsResponse> responseObserver) {
        super.retrieveReactionsByFeedId(request, responseObserver);
    }
}
