package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.AlipayOrderMessage;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.SceneType;
import com.keepreal.madagascar.common.WechatOrderMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.vanga.AlipayOrderResponse;
import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.BalanceResponse;
import com.keepreal.madagascar.vanga.CreateWithdrawRequest;
import com.keepreal.madagascar.vanga.PaymentServiceGrpc;
import com.keepreal.madagascar.vanga.RedirectResponse;
import com.keepreal.madagascar.vanga.RetrieveSupportInfoRequest;
import com.keepreal.madagascar.vanga.RetrieveSupportInfoResponse;
import com.keepreal.madagascar.vanga.RetrieveUserPaymentsRequest;
import com.keepreal.madagascar.vanga.SubscribeMembershipRequest;
import com.keepreal.madagascar.vanga.SupportMessage;
import com.keepreal.madagascar.vanga.SupportRequest;
import com.keepreal.madagascar.vanga.UserPaymentsResponse;
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
     * @param remoteAddress   Remote address ip.
     * @param membershipSkuId Membership sku id.
     */
    public WechatOrderMessage submitSubscribeMembershipWithWechatPay(String userId, String remoteAddress, String membershipSkuId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        SubscribeMembershipRequest request = SubscribeMembershipRequest.newBuilder()
                .setUserId(userId)
                .setIpAddress(remoteAddress)
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

    /**
     * Submit a membership subscription request for a given user with wechat pay.
     *
     * @param userId          User id.
     * @param remoteAddress   Remote address ip.
     * @param membershipSkuId Membership sku id.
     * @param sceneType       Scene type.
     * @return {@link RedirectResponse}.
     */
    public RedirectResponse submitSubscribeMembershipWithWechatPayH5(String userId, String remoteAddress, String membershipSkuId, SceneType sceneType) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        SubscribeMembershipRequest request = SubscribeMembershipRequest.newBuilder()
                .setUserId(userId)
                .setIpAddress(remoteAddress)
                .setMembershipSkuId(membershipSkuId)
                .setSceneType(sceneType)
                .build();

        RedirectResponse response;
        try {
            response = stub.submitSubscribeMembershipWithWechatPayH5(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Create wechat h5 order returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Submit one time support with wechat pay.
     *
     * @param userId        Payer id.
     * @param payeeId       Payee id.
     * @param sponsorSkuId  Sponsor sku id.
     * @param priceInCents  Price in cents.
     * @param priceInShells Price in shells.
     * @param ipAddress     Payer ip address.
     * @return {@link WechatOrderMessage}.
     */
    public WechatOrderMessage submitSupportWithWechatPay(String userId, String payeeId, String sponsorSkuId, Long priceInCents, Long priceInShells, String ipAddress) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        SupportRequest request = SupportRequest.newBuilder()
                .setUserId(userId)
                .setPayeeId(payeeId)
                .setSponsorSkuId(sponsorSkuId)
                .setPriceInCents(priceInCents)
                .setPriceInShells(priceInShells)
                .setIpAddress(ipAddress)
                .build();

        WechatOrderResponse response;

        try {
            response = stub.submitSupportWithWechatPay(request);
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

    public RedirectResponse submitSupportWithWechatPayH5(String userId,
                                                         String payeeId,
                                                         String sponsorSkuId,
                                                         Long priceInCents,
                                                         Long priceInShells,
                                                         String ipAddress,
                                                         SceneType sceneType) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        SupportRequest request = SupportRequest.newBuilder()
                .setUserId(userId)
                .setPayeeId(payeeId)
                .setSponsorSkuId(sponsorSkuId)
                .setPriceInCents(priceInCents)
                .setPriceInShells(priceInShells)
                .setIpAddress(ipAddress)
                .setSceneType(sceneType)
                .build();

        RedirectResponse response;
        try {
            response = stub.submitSupportWithWechatPayH5(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Create wechat h5 order returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Retrieves the user payment history.
     *
     * @param userId   User id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link UserPaymentsResponse}.
     */
    public UserPaymentsResponse retrieveUserPayments(String userId, Integer page, Integer pageSize) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        RetrieveUserPaymentsRequest request = RetrieveUserPaymentsRequest.newBuilder()
                .setUserId(userId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        UserPaymentsResponse response;
        try {
            response = stub.retrieveUserPayments(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve user payment history returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    public SupportMessage retrieveSupportInfo(String userId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        RetrieveSupportInfoRequest request = RetrieveSupportInfoRequest.newBuilder()
                .setHostId(userId)
                .build();

        RetrieveSupportInfoResponse response;

        try {
            response = stub.retrieveSupportInfo(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve support info returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMessage();
    }

    /**
     * Submit a membership subscription request for a given user with ali pay.
     *
     * @param userId          User id.
     * @param membershipSkuId Membership sku id.
     */
    public AlipayOrderMessage submitSubscribeMembershipWithAlipay(String userId, String membershipSkuId) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        SubscribeMembershipRequest request = SubscribeMembershipRequest.newBuilder()
                .setUserId(userId)
                .setMembershipSkuId(membershipSkuId)
                .build();

        AlipayOrderResponse response;
        try {
            response = stub.submitSubscribeMembershipWithAlipay(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Create ali pay order returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getAlipayOrder();
    }

    /**
     * Submit a one time support request for a given user with ali pay.
     *
     * @param userId        User id.
     * @param payeeId       Membership sku id.
     * @param priceInCents  Price in cents.
     * @param sponsorSkuId  Sponsor sku id.
     */
    public AlipayOrderMessage submitSupportWithAlipay(String userId, String payeeId, String sponsorSkuId, Long priceInCents) {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = PaymentServiceGrpc.newBlockingStub(this.channel);

        SupportRequest request = SupportRequest.newBuilder()
                .setUserId(userId)
                .setPayeeId(payeeId)
                .setSponsorSkuId(sponsorSkuId)
                .setPriceInCents(priceInCents)
                .build();

        AlipayOrderResponse response;
        try {
            response = stub.submitSupportWithAlipay(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Create alipay order returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getAlipayOrder();
    }

}
