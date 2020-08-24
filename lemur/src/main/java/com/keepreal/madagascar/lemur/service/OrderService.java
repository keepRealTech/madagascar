package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.IOSOrderBuyShellRequest;
import com.keepreal.madagascar.vanga.IOSOrderSubscribeRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveWechatOrderByIdRequest;
import com.keepreal.madagascar.vanga.WechatOrderBuyShellRequest;
import com.keepreal.madagascar.vanga.WechatOrderCallbackRequest;
import com.keepreal.madagascar.vanga.WechatOrderMessage;
import com.keepreal.madagascar.vanga.WechatOrderResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the order service.
 */
@Service
@Slf4j
public class OrderService {

    private final Channel channel;

    /**
     * Constructs the payment service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public OrderService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Retrieves wechat order by id.
     *
     * @param id Order id.
     * @return {@link WechatOrderMessage}.
     */
    public WechatOrderMessage retrieveWechatOrderById(String id) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        RetrieveWechatOrderByIdRequest request = RetrieveWechatOrderByIdRequest.newBuilder()
                .setId(id)
                .build();

        WechatOrderResponse response;
        try {
            response = stub.retrieveWechatOrderById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Create wechat order returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getWechatOrder();
    }

    /**
     * Calls back on wechat order notification.
     *
     * @param payload Payload.
     */
    public void wechatOrderCallback(String payload) {
        PaymentServiceGrpc.PaymentServiceFutureStub stub = PaymentServiceGrpc.newFutureStub(this.channel);

        WechatOrderCallbackRequest request = WechatOrderCallbackRequest.newBuilder()
                .setPayload(payload)
                .build();

        stub.wechatPayCallback(request);
    }

    /**
     * Calls back on wechat order refund notification.
     *
     * @param payload Payload.
     */
    public void wechatOrderRefundCallback(String payload) {
        PaymentServiceGrpc.PaymentServiceFutureStub stub = PaymentServiceGrpc.newFutureStub(this.channel);

        WechatOrderCallbackRequest request = WechatOrderCallbackRequest.newBuilder()
                .setPayload(payload)
                .build();

        stub.wechatRefundCallback(request);
    }

    /**
     * Buys shell with ios receipt.
     *
     * @param userId        User id.
     * @param shellSkuId    Shell sku id.
     * @param receipt       Receipt content.
     * @param transactionId Transaction id.
     * @return {@link BalanceMessage}.
     */
    public BalanceMessage iosBuyShell(String userId, String shellSkuId, String receipt, String transactionId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        IOSOrderBuyShellRequest request = IOSOrderBuyShellRequest.newBuilder()
                .setUserId(userId)
                .setShellSkuId(shellSkuId)
                .setAppleReceipt(receipt)
                .setTransactionId(Objects.isNull(transactionId) ? "" : transactionId)
                .build();

        BalanceResponse response;
        try {
            response = stub.iOSBuyShell(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Buy shell returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getBalance();
    }

    /**
     * Buys shell with wechat.
     *
     * @param userId     User id.
     * @param openId     Open id.
     * @param shellSkuId Shell sku id.
     * @return {@link WechatOrderMessage}.
     */
    public WechatOrderMessage wechatBuyShell(String userId, String openId, String shellSkuId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        WechatOrderBuyShellRequest request = WechatOrderBuyShellRequest.newBuilder()
                .setUserId(userId)
                .setOpenId(openId)
                .setShellSkuId(shellSkuId)
                .build();

        WechatOrderResponse response;
        try {
            response = stub.wechatBuyShell(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Buy wechat shell returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getWechatOrder();
    }

    /**
     * Subscribes membership with ios receipt.
     *
     * @param userId          User id.
     * @param membershipSkuId Membership sku id.
     * @param receipt         Receipt content.
     * @param transactionId   Transaction id.
     */
    public void iosSubscribeMembership(String userId, String membershipSkuId, String receipt, String transactionId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        IOSOrderSubscribeRequest request = IOSOrderSubscribeRequest.newBuilder()
                .setUserId(userId)
                .setMembershipSkuId(membershipSkuId)
                .setAppleReceipt(receipt)
                .setTransactionId(Objects.isNull(transactionId) ? "" : transactionId)
                .build();

        CommonStatus response;
        try {
            response = stub.iOSSubscribeMembership(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)) {
            log.error("Subscribe membership with ios buy returned null.");
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getRtn()) {
            throw new KeepRealBusinessException(response);
        }
    }

}
