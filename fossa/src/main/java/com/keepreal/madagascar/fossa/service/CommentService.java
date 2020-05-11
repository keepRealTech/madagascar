package com.keepreal.madagascar.fossa.service;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.CommentResponse;
import com.keepreal.madagascar.fossa.CommentServiceGrpc;
import com.keepreal.madagascar.fossa.CommentsResponse;
import com.keepreal.madagascar.fossa.DeleteCommentByIdRequest;
import com.keepreal.madagascar.fossa.DeleteCommentByIdResponse;
import com.keepreal.madagascar.fossa.NewCommentRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentByIdRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentsByFeedIdRequest;
import com.keepreal.madagascar.fossa.config.MqConfig;
import com.keepreal.madagascar.fossa.dao.CommentInfoRepository;
import com.keepreal.madagascar.fossa.model.CommentInfo;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import com.keepreal.madagascar.fossa.util.ProducerUtils;
import com.keepreal.madagascar.tenrecs.CommentEvent;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
import com.keepreal.madagascar.tenrecs.NotificationEventType;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Slf4j
@GRpcService
public class CommentService extends CommentServiceGrpc.CommentServiceImplBase {

    public static final String FEED_COUNT_TYPE = "commentsCount";

    private final CommentInfoRepository commentInfoRepository;
    private final LongIdGenerator idGenerator;
    private final FeedInfoService feedInfoService;
    private final MqConfig mqConfig;
    private final ProducerBean producerBean;

    @Autowired
    public CommentService(CommentInfoRepository commentInfoRepository, LongIdGenerator idGenerator, FeedInfoService feedInfoService, MqConfig mqConfig, ProducerBean producerBean) {
        this.commentInfoRepository = commentInfoRepository;
        this.idGenerator = idGenerator;
        this.feedInfoService = feedInfoService;
        this.mqConfig = mqConfig;
        this.producerBean = producerBean;
    }

    public static CommentMessage getCommentMessage(CommentInfo commentInfo) {
        return CommentMessage.newBuilder()
                .setId(commentInfo.getId())
                .setFeedId(commentInfo.getFeedId())
                .setUserId(commentInfo.getUserId())
                .setContent(commentInfo.getContent())
                .setReplyToId(commentInfo.getReplyToId())
                .setCreatedAt(commentInfo.getCreatedTime())
                .build();
    }

    /**
     * 创建一个comment
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void createComment(NewCommentRequest request, StreamObserver<CommentResponse> responseObserver) {
        String feedId = request.getFeedId();
        String userId = request.getUserId();
        String content = request.getContent();
        String replyToId = request.hasReplyToId() ? request.getReplyToId().getValue() : "";
        CommentInfo commentInfo = new CommentInfo();
        commentInfo.setId(String.valueOf(idGenerator.nextId()));
        commentInfo.setFeedId(feedId);
        commentInfo.setUserId(userId);
        commentInfo.setContent(content);
        commentInfo.setReplyToId(replyToId);
        commentInfo.setDeleted(false);

        CommentInfo save = commentInfoRepository.save(commentInfo);

        feedInfoService.incFeedCount(feedId, FEED_COUNT_TYPE);
        CommentMessage commentMessage = getCommentMessage(save);
        CommentResponse commentResponse = CommentResponse.newBuilder()
                .setComment(commentMessage)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();

        FeedMessage feedMessage = feedInfoService.getFeedMessageById(feedId);
        Message message = getMessage(commentMessage, feedMessage, feedMessage.getUserId());
        ProducerUtils.sendMessageAsync(producerBean, message);
        if (!StringUtils.isEmpty(replyToId)) {
            message = getMessage(commentMessage, feedMessage, replyToId);
            ProducerUtils.sendMessageAsync(producerBean, message);
        }

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
        CommentInfo commentInfo = this.commentInfoRepository.findByIdAndDeletedIsFalse(request.getId());
        if (commentInfo != null) {
            CommentMessage commentMessage = getCommentMessage(commentInfo);
            responseBuilder.setComment(commentMessage)
                    .setStatus(CommonStatusUtils.getSuccStatus());
        } else {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_COMMENT_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * 根据feedId返回这个feed下的所有comment
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveCommentsByFeedId(RetrieveCommentsByFeedIdRequest request, StreamObserver<CommentsResponse> responseObserver) {
        String feedId = request.getFeedId();
        Pageable pageable = PageRequestResponseUtils.getPageableByRequest(request.getPageRequest());
        Page<CommentInfo> commentInfoPage = commentInfoRepository.getCommentInfosByFeedIdAndDeletedIsFalseOrderByCreatedTimeDesc(feedId, pageable);
        List<CommentMessage> commentMessageList = commentInfoPage.getContent()
                .stream().map(CommentService::getCommentMessage)
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
     * 根据commentId删除一条comment
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void deleteCommentById(DeleteCommentByIdRequest request, StreamObserver<DeleteCommentByIdResponse> responseObserver) {
        CommonStatus commonStatus;
        String id = request.getId();
        Optional<CommentInfo> commentInfoOptional = commentInfoRepository.findById(id);
        if (commentInfoOptional.isPresent()) {
            CommentInfo commentInfo = commentInfoOptional.get();
            commentInfo.setDeleted(true);
            commentInfoRepository.save(commentInfo);
            commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
            feedInfoService.subFeedCount(commentInfo.getFeedId(), FEED_COUNT_TYPE);
        } else {
            commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_COMMENT_NOT_FOUND_ERROR);
        }


        DeleteCommentByIdResponse deleteCommentByIdResponse = DeleteCommentByIdResponse
                .newBuilder()
                .setStatus(commonStatus)
                .build();
        responseObserver.onNext(deleteCommentByIdResponse);
        responseObserver.onCompleted();
    }

    private Message getMessage(CommentMessage commentMessage, FeedMessage feedMessage, String userId) {
        CommentEvent commentEvent = CommentEvent.newBuilder()
                .setComment(commentMessage)
                .setFeed(feedMessage)
                .build();
        String uuid = UUID.randomUUID().toString();
        NotificationEvent event = NotificationEvent.newBuilder()
                .setType(NotificationEventType.NOTIFICATION_EVENT_NEW_COMMENT)
                .setUserId(userId)
                .setCommentEvent(commentEvent)
                .setTimestamp(System.currentTimeMillis())
                .setEventId(uuid)
                .build();
        Message message = new Message(mqConfig.getTopic(), mqConfig.getTag(), event.toByteArray());
        message.setKey(uuid);
        return message;
    }
}
