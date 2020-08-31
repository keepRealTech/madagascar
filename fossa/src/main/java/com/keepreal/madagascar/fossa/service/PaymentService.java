package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.WechatOrderMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.ActivatePendingFeedPaymentRequest;
import com.keepreal.madagascar.vanga.CreatePaidFeedRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.RefundWechatFeedRequest;
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

    public WechatOrderMessage wechatCreateFeed(String feedId, long priceInCents, String userId, String hostId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        WechatOrderResponse response;

        try {
            response = stub.wechatCreateFeed(CreatePaidFeedRequest.newBuilder()
                    .setFeedId(feedId)
                    .setPriceInCents(priceInCents)
                    .setUserId(userId)
                    .setHostId(hostId)
                    .setIpAddress("127.0.0.1")
                    .build());
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        return response.getWechatOrder();
    }

    public void refundWechatPaidFeed(String feedId, String userId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        CommonStatus status;

        try {
            status = stub.refundWechatPaidFeed(RefundWechatFeedRequest.newBuilder()
                    .setFeedId(feedId)
                    .setUserId(userId)
                    .build());
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }

    public void activateFeedPayment(String feedId, String userId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        CommonStatus status;

        try {
            status = stub.activateFeedPayment(ActivatePendingFeedPaymentRequest.newBuilder()
                    .setFeedId(feedId)
                    .setUserId(userId)
                    .build());
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }
}
