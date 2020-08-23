package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.UpdateFeedPaidByIdRequest;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Represents the feed service.
 */
@Slf4j
@Service
public class FeedService {

    private final Channel channel;

    /**
     * Constructs the feed service.
     *
     * @param channel Managed channel for grpc traffic.
     */
    public FeedService(@Qualifier("fossaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Confirms a pay question paid.
     *
     */
    public void confirmQuestionPaid(WechatOrder wechatOrder) {

    }

    public void updatePaidQuestion(String feedId) {
        FeedServiceGrpc.FeedServiceFutureStub stub = FeedServiceGrpc.newFutureStub(this.channel);

        UpdateFeedPaidByIdRequest request = UpdateFeedPaidByIdRequest.newBuilder()
                .setId(feedId)
                .build();

        try {
            stub.updateFeedPaidById(request);
        } catch (Exception e) {
            log.error("call comfirm paid question failure! feed id: {}, exception: {}", feedId, e.getMessage());
        }
    }

}
