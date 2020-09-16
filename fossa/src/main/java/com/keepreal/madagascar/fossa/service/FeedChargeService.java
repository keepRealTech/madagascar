package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.FeedChargeServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveFeedChargeRequest;
import com.keepreal.madagascar.vanga.RetrieveFeedChargeResponse;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FeedChargeService {

    private final Channel channel;

    public FeedChargeService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    public boolean retrieveFeedChargeAccess(String userId, String feedId) {
        FeedChargeServiceGrpc.FeedChargeServiceBlockingStub stub = FeedChargeServiceGrpc.newBlockingStub(this.channel);

        RetrieveFeedChargeResponse response;

        try {
            response = stub.retrieveFeedChargeAccess(RetrieveFeedChargeRequest.newBuilder()
                    .setUserId(userId)
                    .setFeedId(feedId)
                    .build());
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        return response.getHasAccess();
    }
}
