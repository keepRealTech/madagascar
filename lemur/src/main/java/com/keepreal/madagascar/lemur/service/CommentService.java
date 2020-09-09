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
import com.keepreal.madagascar.fossa.RetrieveCommentByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentsByIdsRequest;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.List;

/**
 * Represents the comment service.
 */
@Service
@Slf4j
public class CommentService {

    private final Channel channel;

    /**
     * Constructs the comment service.
     *
     * @param channel GRpc managed channel connection to service Fossa.
     */
    public CommentService(@Qualifier("fossaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Retrieves a comment by id.
     *
     * @param id Comment id.
     * @return {@link CommentMessage}.
     */
    public CommentMessage retrieveCommentById(String id) {
        CommentServiceGrpc.CommentServiceBlockingStub stub = CommentServiceGrpc.newBlockingStub(this.channel);

        RetrieveCommentByIdRequest request = RetrieveCommentByIdRequest.newBuilder()
                .setId(id)
                .build();

        CommentResponse commentResponse;
        try {
            commentResponse = stub.retrieveCommentById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(commentResponse)
                || !commentResponse.hasStatus()) {
            log.error(Objects.isNull(commentResponse) ? "Retrieve comment returned null." : commentResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != commentResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(commentResponse.getStatus());
        }

        return commentResponse.getComment();
    }

    /**
     * Retrieves comments by ids.
     *
     * @param ids Comment ids.
     * @return {@link CommentMessage}.
     */
    public List<CommentMessage> retrieveCommentByIds(Iterable<String> ids) {
        CommentServiceGrpc.CommentServiceBlockingStub stub = CommentServiceGrpc.newBlockingStub(this.channel);

        RetrieveCommentsByIdsRequest request = RetrieveCommentsByIdsRequest.newBuilder()
                .addAllIds(ids)
                .build();

        CommentsResponse commentsResponse;
        try {
            commentsResponse = stub.retrieveCommentsByIds(request);
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

        return commentsResponse.getCommentsList();
    }

    /**
     * Deletes a comment by id.
     *
     * @param id Comment id.
     */
    public void deleteCommentById(String id) {
        CommentServiceGrpc.CommentServiceBlockingStub stub = CommentServiceGrpc.newBlockingStub(this.channel);

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
        CommentServiceGrpc.CommentServiceBlockingStub stub = CommentServiceGrpc.newBlockingStub(this.channel);

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
        CommentServiceGrpc.CommentServiceBlockingStub stub = CommentServiceGrpc.newBlockingStub(this.channel);

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
