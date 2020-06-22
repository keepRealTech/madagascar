package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.IOSOrderBuyShellRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.RetrieveWechatOrderByIdRequest;
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
     * Buys shell with ios receipt.
     *
     * @param userId     User id.
     * @param shellSkuId Shell sku id.
     * @param receipt    Receipt content.
     * @return {@link BalanceMessage}.
     */
    public BalanceMessage iosBuyShell(String userId, String shellSkuId, String receipt) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        IOSOrderBuyShellRequest request = IOSOrderBuyShellRequest.newBuilder()
                .setUserId(userId)
                .setShellSkuId(shellSkuId)
                .setAppleReceipt(receipt)
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

}
