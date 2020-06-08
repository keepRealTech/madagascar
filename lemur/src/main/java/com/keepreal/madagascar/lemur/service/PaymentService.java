package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.CreateWithdrawRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.SubscribeMembershipRequest;
import com.keepreal.madagascar.vanga.WechatOrderMessage;
import com.keepreal.madagascar.vanga.WechatOrderResponse;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the payment service.
 */
@Service
@Slf4j
public class PaymentService {

    private final Channel channel;

    /**
     * Constructs the payment service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public PaymentService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Submits a withdraw request.
     *
     * @param userId        User id.
     * @param amountInCents Amount in cents try to withdraw.
     * @return {@link BalanceMessage}.
     */
    public BalanceMessage submitWithdrawRequest(String userId, Long amountInCents) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        CreateWithdrawRequest request = CreateWithdrawRequest.newBuilder()
                .setUserId(userId)
                .setWithdrawAmountInCents(amountInCents)
                .build();

        BalanceResponse balanceResponse;
        try {
            balanceResponse = stub.createWithdrawPayment(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(balanceResponse)
                || !balanceResponse.hasStatus()) {
            log.error(Objects.isNull(balanceResponse) ? "Create withdraw request returned null." : balanceResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != balanceResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(balanceResponse.getStatus());
        }

        return balanceResponse.getBalance();
    }

    /**
     * Subscribe membership for a given user with its shell balance.
     *
     * @param userId          User id.
     * @param membershipSkuId Membership sku id.
     */
    public void subscribeMembershipWithShell(String userId, String membershipSkuId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        SubscribeMembershipRequest request = SubscribeMembershipRequest.newBuilder()
                .setUserId(userId)
                .setMembershipSkuId(membershipSkuId)
                .build();

        CommonStatus response;
        try {
            response = stub.subscribeMembershipWithShell(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        } else if (ErrorCode.REQUEST_SUCC_VALUE != response.getRtn()) {
            throw new KeepRealBusinessException(response);
        }
    }

    /**
     * Submit a membership subscription request for a given user with wechat pay.
     *
     * @param userId          User id.
     * @param membershipSkuId Membership sku id.
     */
    public WechatOrderMessage submitSubscribeMembershipWithWechatPay(String userId, String membershipSkuId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        SubscribeMembershipRequest request = SubscribeMembershipRequest.newBuilder()
                .setUserId(userId)
                .setMembershipSkuId(membershipSkuId)
                .build();

        WechatOrderResponse response;
        try {
            response = stub.submitSubscribeMembershipWithWechatPay(request);
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

}
