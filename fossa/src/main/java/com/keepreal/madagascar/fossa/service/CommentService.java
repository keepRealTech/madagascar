package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.PageResponse;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.fossa.CommentResponse;
import com.keepreal.madagascar.fossa.CommentServiceGrpc;
import com.keepreal.madagascar.fossa.CommentsResponse;
import com.keepreal.madagascar.fossa.DeleteCommentByIdRequest;
import com.keepreal.madagascar.fossa.DeleteCommentByIdResponse;
import com.keepreal.madagascar.fossa.NewCommentRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentsByFeedIdRequest;
import com.keepreal.madagascar.fossa.dao.CommentInfoRepository;
import com.keepreal.madagascar.fossa.model.CommentInfo;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import com.keepreal.madagascar.fossa.util.PageRequestResponseUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@GRpcService
public class CommentService extends CommentServiceGrpc.CommentServiceImplBase {

    private final CommentInfoRepository commentInfoRepository;

    @Autowired
    public CommentService(CommentInfoRepository commentInfoRepository) {
        this.commentInfoRepository = commentInfoRepository;
    }

    /**
     * 创建一个comment
     * @param request
     * @param responseObserver
     */
    @Override
    public void createComment(NewCommentRequest request, StreamObserver<CommentResponse> responseObserver) {
        String feedId = request.getFeedId();
        String userId = request.getUserId();
        String content = request.getContent();
        String replyToId = userId;//todo
        if (request.hasReplyToId()) {
            replyToId = request.getReplyToId().getValue();
        }
        CommentInfo commentInfo = new CommentInfo();
        commentInfo.setFeedId(Long.valueOf(feedId));
        commentInfo.setUserId(Long.valueOf(userId));
        commentInfo.setContent(content);
        commentInfo.setReplyToId(Long.valueOf(replyToId));
        commentInfo.setDeleted(false);
        commentInfo.setCreatedTime(System.currentTimeMillis());
        commentInfo.setUpdatedTime(System.currentTimeMillis());

        CommentInfo save = commentInfoRepository.save(commentInfo);

        CommentMessage commentMessage = getCommentMessage(save);
        CommentResponse commentResponse = CommentResponse.newBuilder()
                .setComment(commentMessage).build();
        responseObserver.onNext(commentResponse);
        responseObserver.onCompleted();
        super.createComment(request, responseObserver);
    }

    /**
     * 根据feedId返回这个feed下的所有comment
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveCommentsByFeedId(RetrieveCommentsByFeedIdRequest request, StreamObserver<CommentsResponse> responseObserver) {
        String feedId = request.getFeedId();
        Pageable pageable = PageRequestResponseUtils.getPageableByRequest(request.getPageRequest());
        Page<CommentInfo> commentInfoPage = commentInfoRepository.getCommentInfosByFeedIdAndDeletedIsFalse(Long.valueOf(feedId), pageable);
        List<CommentMessage> commentMessageList = commentInfoPage.getContent()
                .stream().map(this::getCommentMessage)
                .collect(Collectors.toList());

        PageResponse pageResponse = PageRequestResponseUtils.buildPageResponse(commentInfoPage);
        CommentsResponse commentsResponse = CommentsResponse.newBuilder()
                .addAllComments(commentMessageList)
                .setPageResponse(pageResponse)
                .build();

        responseObserver.onNext(commentsResponse);
        responseObserver.onCompleted();
    }

    /**
     * 根据commentId删除一条comment
     * @param request
     * @param responseObserver
     */
    @Override
    public void deleteCommentById(DeleteCommentByIdRequest request, StreamObserver<DeleteCommentByIdResponse> responseObserver) {
        CommonStatus commonStatus;
        String id = request.getId();
        Optional<CommentInfo> commentInfoOptional = commentInfoRepository.findById(Long.valueOf(id));
        if (commentInfoOptional.isPresent()) {
            CommentInfo commentInfo = commentInfoOptional.get();
            commentInfo.setDeleted(true);
            commentInfoRepository.save(commentInfo);
            commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
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

    private CommentMessage getCommentMessage(CommentInfo commentInfo) {
        return CommentMessage.newBuilder()
                .setId(commentInfo.getId().toString())
                .setFeedId(commentInfo.getFeedId().toString())
                .setUserId(commentInfo.getUserId().toString())
                .setContent(commentInfo.getContent())
                .setReplyToId(commentInfo.getReplyToId().toString())
                .setCreatedAt(commentInfo.getCreatedTime())
                .build();
    }
}
