package com.keepreal.madagascar.marty.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.RetrieveFeedByIdRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FeedService {

    private final Channel channel;

    public FeedService(@Qualifier("fossaChannel") Channel channel) {
        this.channel = channel;
    }

    public String retrieveFeedTextById(String feedId, String userId) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(channel);

        FeedResponse feedResponse;
        try {
            feedResponse = stub.retrieveFeedById(RetrieveFeedByIdRequest.newBuilder()
                    .setId(feedId)
                    .setUserId(userId)
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        return feedResponse.getFeed().getText();
    }
}
