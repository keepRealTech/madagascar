package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.CommentServiceGrpc;
import com.keepreal.madagascar.fossa.DeleteCommentByIdRequest;
import com.keepreal.madagascar.fossa.DeleteCommentByIdResponse;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
            throw new KeepRealBusinessException(exception);
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

}
