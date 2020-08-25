package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.WechatOrderMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.CreatePaidFeedRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.WechatOrderResponse;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final Channel channel;

    public PaymentService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    public WechatOrderMessage wechatCreateFeed(String feedId, long priceInCents) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        WechatOrderResponse response;

        try {
            response = stub.wechatCreateFeed(CreatePaidFeedRequest.newBuilder()
                    .setFeedId(feedId)
                    .setPriceInCents(priceInCents)
                    .build());
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        return response.getWechatOrder();
    }
}