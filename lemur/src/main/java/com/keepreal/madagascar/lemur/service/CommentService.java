package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.CommentResponse;
import com.keepreal.madagascar.fossa.CommentServiceGrpc;
import com.keepreal.madagascar.fossa.CommentsResponse;
import com.keepreal.madagascar.fossa.DeleteCommentByIdRequest;
import com.keepreal.madagascar.fossa.DeleteCommentByIdResponse;
import com.keepreal.madagascar.fossa.NewCommentRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentsByFeedIdRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Represents the comment service.
 */
@Service
@Slf4j
public class CommentService {

    private final ManagedChannel managedChannel;

    /**
     * Constructs the comment service.
     *
     * @param managedChannel GRpc managed channel connection to service Fossa.
     */
    public CommentService(@Qualifier("fossaChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * Deletes a comment by id.
     *
     * @param id Comment id.
     */
    public void deleteCommentById(String id) {
        CommentServiceGrpc.CommentServiceBlockingStub stub = CommentServiceGrpc.newBlockingStub(this.managedChannel);

        DeleteCommentByIdRequest request = DeleteCommentByIdRequest.newBuilder()
                .setId(id)
                .build();

        DeleteCommentByIdResponse deleteCommentByIdResponse;
        try {
            deleteCommentByIdResponse = stub.deleteCommentById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(deleteCommentByIdResponse)
                || !deleteCommentByIdResponse.hasStatus()) {
            log.error(Objects.isNull(deleteCommentByIdResponse) ? "Delete comment returned null." : deleteCommentByIdResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != deleteCommentByIdResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(deleteCommentByIdResponse.getStatus());
        }
    }

    /**
     * Creates a comment.
     *
     * @param feedId    Feed id.
     * @param userId    User id.
     * @param content   Content.
     * @param replyToId Reply to user id.
     * @return {@link CommentMessage}.
     */
    public CommentMessage createComment(String feedId, String userId, String content, String replyToId) {
        CommentServiceGrpc.CommentServiceBlockingStub stub = CommentServiceGrpc.newBlockingStub(this.managedChannel);

        NewCommentRequest.Builder requestBuilder = NewCommentRequest.newBuilder()
                .setFeedId(feedId)
                .setUserId(userId)
                .setContent(content);

        if (!StringUtils.isEmpty(replyToId)) {
            requestBuilder.setReplyToId(StringValue.of(replyToId));
        }

        CommentResponse commentResponse;
        try {
            commentResponse = stub.createComment(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(commentResponse)
                || !commentResponse.hasStatus()) {
            log.error(Objects.isNull(commentResponse) ? "Create comment returned null." : commentResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != commentResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(commentResponse.getStatus());
        }

        return commentResponse.getComment();
    }

    /**
     * Retrieves comments by feed id.
     *
     * @param feedId   Feed id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link CommentsResponse}.
     */
    public CommentsResponse retrieveCommentsByFeedId(String feedId, int page, int pageSize) {
        CommentServiceGrpc.CommentServiceBlockingStub stub = CommentServiceGrpc.newBlockingStub(this.managedChannel);

        RetrieveCommentsByFeedIdRequest request = RetrieveCommentsByFeedIdRequest.newBuilder()
                .setFeedId(feedId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        CommentsResponse commentsResponse;
        try {
            commentsResponse = stub.retrieveCommentsByFeedId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(commentsResponse)
                || !commentsResponse.hasStatus()) {
            log.error(Objects.isNull(commentsResponse) ? "Retrieve comments returned null." : commentsResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != commentsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(commentsResponse.getStatus());
        }

        return commentsResponse;
    }

}
