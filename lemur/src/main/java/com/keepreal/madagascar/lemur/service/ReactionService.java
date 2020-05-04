package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.ReactionMessage;
import com.keepreal.madagascar.common.ReactionType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.NewReactionRequest;
import com.keepreal.madagascar.fossa.ReactionResponse;
import com.keepreal.madagascar.fossa.ReactionServiceGrpc;
import com.keepreal.madagascar.fossa.ReactionsResponse;
import com.keepreal.madagascar.fossa.RetrieveReactionsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RevokeReactionRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the reaction service.
 */
@Service
@Slf4j
public class ReactionService {

    private final ManagedChannel managedChannel;

    /**
     * Constructs the reaction service.
     *
     * @param managedChannel GRpc managed channel connection to service Fossa.
     */
    public ReactionService(@Qualifier("fossaChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * Creates a reaction.
     *
     * @param feedId Feed id.
     * @param userId User id.
     * @param types  {@link ReactionType}.
     * @return {@link ReactionMessage}.
     */
    public ReactionMessage createReaction(String feedId, String userId, List<ReactionType> types) {
        ReactionServiceGrpc.ReactionServiceBlockingStub stub = ReactionServiceGrpc.newBlockingStub(this.managedChannel);

        NewReactionRequest request = NewReactionRequest.newBuilder()
                .setFeedId(feedId)
                .setUserId(userId)
                .addAllReactionTypes(types)
                .build();

        ReactionResponse reactionResponse;
        try {
            reactionResponse = stub.createReaction(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(reactionResponse)
                || !reactionResponse.hasStatus()) {
            log.error(Objects.isNull(reactionResponse) ? "Create comment returned null." : reactionResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != reactionResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(reactionResponse.getStatus());
        }

        return reactionResponse.getReaction();
    }

    /**
     * Revokes a reaction.
     *
     * @param feedId Feed id.
     * @param userId User id.
     * @param types  {@link ReactionType}.
     * @return {@link ReactionMessage}.
     */
    public ReactionMessage revokeReaction(String feedId, String userId, List<ReactionType> types) {
        ReactionServiceGrpc.ReactionServiceBlockingStub stub = ReactionServiceGrpc.newBlockingStub(this.managedChannel);

        RevokeReactionRequest request = RevokeReactionRequest.newBuilder()
                .setFeedId(feedId)
                .setUserId(userId)
                .addAllReactionTypes(types)
                .build();

        ReactionResponse reactionResponse;
        try {
            reactionResponse = stub.revokeReaction(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(reactionResponse)
                || !reactionResponse.hasStatus()) {
            log.error(Objects.isNull(reactionResponse) ? "Create comment returned null." : reactionResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != reactionResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(reactionResponse.getStatus());
        }

        return reactionResponse.getReaction();
    }

    /**
     * Retrieves reactions by feed id.
     *
     * @param feedId   Feed id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link ReactionsResponse}.
     */
    public ReactionsResponse retrieveReactionsByFeedId(String feedId, int page, int pageSize) {
        ReactionServiceGrpc.ReactionServiceBlockingStub stub = ReactionServiceGrpc.newBlockingStub(this.managedChannel);

        RetrieveReactionsByFeedIdRequest request = RetrieveReactionsByFeedIdRequest.newBuilder()
                .setFeedId(feedId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        ReactionsResponse reactionsResponse;
        try {
            reactionsResponse = stub.retrieveReactionsByFeedId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(reactionsResponse)
                || !reactionsResponse.hasStatus()) {
            log.error(Objects.isNull(reactionsResponse) ? "Retrieve reactions returned null." : reactionsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != reactionsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(reactionsResponse.getStatus());
        }

        return reactionsResponse;
    }

}
