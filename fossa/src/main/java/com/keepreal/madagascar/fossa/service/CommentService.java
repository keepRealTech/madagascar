package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.fossa.CommentResponse;
import com.keepreal.madagascar.fossa.CommentServiceGrpc;
import com.keepreal.madagascar.fossa.CommentsResponse;
import com.keepreal.madagascar.fossa.DeleteCommentByIdRequest;
import com.keepreal.madagascar.fossa.DeleteCommentByIdResponse;
import com.keepreal.madagascar.fossa.NewCommentRequest;
import com.keepreal.madagascar.fossa.RetrieveCommentsByFeedIdRequest;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@GRpcService
public class CommentService extends CommentServiceGrpc.CommentServiceImplBase {

    @Override
    public void createComment(NewCommentRequest request, StreamObserver<CommentResponse> responseObserver) {
        super.createComment(request, responseObserver);
    }

    @Override
    public void retrieveCommentsByFeedId(RetrieveCommentsByFeedIdRequest request, StreamObserver<CommentsResponse> responseObserver) {
        super.retrieveCommentsByFeedId(request, responseObserver);
    }

    @Override
    public void deleteCommentById(DeleteCommentByIdRequest request, StreamObserver<DeleteCommentByIdResponse> responseObserver) {
        super.deleteCommentById(request, responseObserver);
    }
}
