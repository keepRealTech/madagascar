package com.keepreal.madagascar.fossa.grpcController;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.fossa.CommentResponse;
import com.keepreal.madagascar.fossa.CommentServiceGrpc;
import com.keepreal.madagascar.fossa.CommentsResponse;
import com.keepreal.madagascar.fossa.DeleteCommentByIdRequest;
import com.keepreal.madagascar.fossa.DeleteCommentByIdResponse;
import com.keepreal.madagascar.fossa.NewCommentRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentsByFeedIdRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentsByIdsRequest;
import com.keepreal.madagascar.fossa.common.FeedCountType;
import com.keepreal.madagascar.fossa.config.NotificationEventProducerConfiguration;
import com.keepreal.madagascar.fossa.model.CommentInfo;
import com.keepreal.madagascar.fossa.service.CommentService;
import com.keepreal.madagascar.fossa.service.FeedInfoService;
import com.keepreal.madagascar.fossa.service.NotificationEventProducerService;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents comment GRpc controller
 */
@Slf4j
@GRpcService
public class CommentGRpcController extends CommentServiceGrpc.CommentServiceImplBase {

    private final FeedInfoService feedInfoService;
    private final CommentService commentService;
    private final NotificationEventProducerService notificationEventProducerService;

    /**
     * Constructs comment grpc controller.
     *
     * @param feedInfoService                  {@link FeedInfoService}.
     * @param commentService                   {@link CommentService}.
     * @param notificationEventProducerService {@link NotificationEventProducerConfiguration}.
     */
    public CommentGRpcController(FeedInfoService feedInfoService,
                                 CommentService commentService,
                                 NotificationEventProducerService notificationEventProducerService) {
        this.feedInfoService = feedInfoService;
        this.commentService = commentService;
        this.notificationEventProducerService = notificationEventProducerService;
    }

    /**
     * implements the create comment method
     *
     * @param request          {@link NewCommentRequest}.
     * @param responseObserver {@link CommentResponse} Callback.
     */
    @Override
    public void createComment(NewCommentRequest request, StreamObserver<CommentResponse> responseObserver) {
        String feedId = request.getFeedId();
        String userId = request.getUserId();
        String content = request.getContent();
        String replyToId = request.hasReplyToId() ? request.getReplyToId().getValue() : "";
        CommentInfo commentInfo = new CommentInfo();
        commentInfo.setFeedId(feedId);
        commentInfo.setUserId(userId);
        commentInfo.setContent(content);
        commentInfo.setReplyToId(replyToId);

        CommentInfo save = this.commentService.insert(commentInfo);

        this.feedInfoService.incFeedCount(feedId, FeedCountType.COMMENTS_COUNT);
        CommentMessage commentMessage = this.commentService.getCommentMessage(save);
        CommentResponse commentResponse = CommentResponse.newBuilder()
                .setComment(commentMessage)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();

        FeedMessage feedMessage = this.feedInfoService.getFeedMessageById(feedId, userId);

        this.notificationEventProducerService.produceNewCommentsNotificationEventAsync(
                commentMessage, feedMessage, replyToId);

        responseObserver.onNext(commentResponse);
        responseObserver.onCompleted();
    }

    /**
     * Retrieves comment by id.
     *
     * @param request          {@link RetrieveCommentByIdRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveCommentById(RetrieveCommentByIdRequest request, StreamObserver<CommentResponse> responseObserver) {
        CommentResponse.Builder responseBuilder = CommentResponse.newBuilder();
        CommentInfo commentInfo = commentService.findByIdAndDeletedIsFalse(request.getId());
        if (commentInfo != null) {
            CommentMessage commentMessage = commentService.getCommentMessage(commentInfo);
            responseBuilder.setComment(commentMessage)
                    .setStatus(CommonStatusUtils.getSuccStatus());
        } else {
            log.error("[retrieveCommentById] comment not found error! comment id is [{}]", request.getId());
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_COMMENT_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * Retrieves comment by id.
     *
     * @param request          {@link RetrieveCommentsByIdsRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveCommentsByIds(RetrieveCommentsByIdsRequest request, StreamObserver<CommentsResponse> responseObserver) {
        CommentsResponse.Builder responseBuilder = CommentsResponse.newBuilder();
        List<CommentInfo> commentInfo = commentService.findByIdsAndDeletedIsFalse(request.getIdsList());

        responseBuilder.addAllComments(commentInfo.stream().map(this.commentService::getCommentMessage).collect(Collectors.toList()))
                .setStatus(CommonStatusUtils.getSuccStatus());

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * Retrieves comments by feedId.
     *
     * @param request          {@link RetrieveCommentsByFeedIdRequest}.
     * @param responseObserver {@link CommentsResponse}.
     */
    @Override
    public void retrieveCommentsByFeedId(RetrieveCommentsByFeedIdRequest request, StreamObserver<CommentsResponse> responseObserver) {
        String feedId = request.getFeedId();
        Pageable pageable = PageRequestResponseUtils.getPageableByRequest(request.getPageRequest());
        Page<CommentInfo> commentInfoPage = commentService.getCommentInfosByFeedId(feedId, pageable);
        List<CommentMessage> commentMessageList = commentInfoPage.getContent()
                .stream().map(commentService::getCommentMessage)
                .collect(Collectors.toList());

        PageResponse pageResponse = PageRequestResponseUtils.buildPageResponse(commentInfoPage);
        CommentsResponse commentsResponse = CommentsResponse.newBuilder()
                .addAllComments(commentMessageList)
                .setPageResponse(pageResponse)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();

        responseObserver.onNext(commentsResponse);
        responseObserver.onCompleted();
    }

    /**
     * Delete comment by id
     *
     * @param request          {@link DeleteCommentByIdRequest}.
     * @param responseObserver {@link DeleteCommentByIdResponse}.
     */
    @Override
    public void deleteCommentById(DeleteCommentByIdRequest request, StreamObserver<DeleteCommentByIdResponse> responseObserver) {
        CommonStatus commonStatus;
        String id = request.getId();
        CommentInfo commentInfo = commentService.findByIdAndDeletedIsFalse(id);
        if (commentInfo != null) {
            commentInfo.setDeleted(true);
            commentService.update(commentInfo);
            feedInfoService.subFeedCount(commentInfo.getFeedId(), FeedCountType.COMMENTS_COUNT);
            commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        } else {
            log.error("[deleteCommentById] comment not found error! comment id is [{}]", request.getId());
            commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_COMMENT_NOT_FOUND_ERROR);
        }

        DeleteCommentByIdResponse deleteCommentByIdResponse = DeleteCommentByIdResponse
                .newBuilder()
                .setStatus(commonStatus)
                .build();
        responseObserver.onNext(deleteCommentByIdResponse);
        responseObserver.onCompleted();
    }

}
