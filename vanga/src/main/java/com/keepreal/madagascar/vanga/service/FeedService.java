package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.FeedMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.FeedResponse;
import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.RetrieveFeedByIdRequest;
import com.keepreal.madagascar.fossa.UpdateFeedPaidByIdRequest;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.OrderState;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Objects;

/**
 * Represents the feed service.
 */
@Slf4j
@Service
public class FeedService {

    private final Channel channel;
    private final PaymentService paymentService;
    private final WechatOrderService wechatOrderService;
    private final WechatPayService wechatPayService;
    private final BalanceService balanceService;
    private final NotificationEventProducerService notificationEventProducerService;

    /**
     * Constructs the feed service.
     *
     * @param channel            Managed channel for grpc traffic.
     * @param paymentService     {@link PaymentService}.
     * @param wechatOrderService {@link WechatOrderService}.
     * @param wechatPayService   {@link WechatPayService}.
     * @param balanceService     {@link BalanceService}.
     * @param notificationEventProducerService {@link NotificationEventProducerService}
     */
    public FeedService(@Qualifier("fossaChannel") Channel channel,
                       PaymentService paymentService,
                       WechatOrderService wechatOrderService,
                       WechatPayService wechatPayService,
                       BalanceService balanceService,
                       NotificationEventProducerService notificationEventProducerService) {
        this.channel = channel;
        this.paymentService = paymentService;
        this.wechatOrderService = wechatOrderService;
        this.wechatPayService = wechatPayService;
        this.balanceService = balanceService;
        this.notificationEventProducerService = notificationEventProducerService;
    }

    /**
     * Confirms a pay question paid.
     *
     * @param wechatOrder {@link WechatOrder}.
     */
    @Transactional
    public void confirmQuestionPaid(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder) || OrderState.SUCCESS.getValue() != wechatOrder.getState()) {
            return;
        }

        Payment payment = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId()).stream().findFirst().orElse(null);

        if (Objects.isNull(payment) || PaymentState.DRAFTED.getValue() != payment.getState()) {
            return;
        }

        Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(payment.getPayeeId());

        ZonedDateTime currentTimestamp = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        payment.setWithdrawPercent(hostBalance.getWithdrawPercent());
        payment.setState(PaymentState.PENDING.getValue());
        payment.setValidAfter(currentTimestamp
                .plusMonths(SubscribeMembershipService.PAYMENT_SETTLE_IN_MONTH)
                .toInstant().toEpochMilli());

        this.notificationEventProducerService.produceNewBalanceNotificationEventAsync(hostBalance.getUserId(), payment.getAmountInCents());
        this.balanceService.addOnCents(hostBalance, this.calculateAmount(payment.getAmountInCents(), hostBalance.getWithdrawPercent()));
        this.paymentService.updateAll(Collections.singletonList(payment));
        this.updatePaidQuestion(wechatOrder.getPropertyId());
    }

    /**
     * Refunds the unsettled payment for given question feed.
     *
     * @param feedId Feed id.
     * @param userId User id.
     */
    @Transactional
    public void refundQuestionPaid(String feedId, String userId) {
        WechatOrder wechatOrder = this.wechatOrderService.retrieveByQuestionId(feedId);

        if (Objects.isNull(wechatOrder)) {
            log.error("Refund feed {} error, no wechat order found.", feedId);
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        Payment payment = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId()).stream().findFirst().orElse(null);

        if (Objects.isNull(payment) || PaymentState.PENDING.getValue() != payment.getState()) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR);
        }

        if (!payment.getUserId().equals(userId)
                && !payment.getPayeeId().equals(userId)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_FORBIDDEN);
        }

        Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(payment.getPayeeId());
        payment.setState(PaymentState.REFUNDING.getValue());

        this.balanceService.subtractCents(hostBalance, this.calculateAmount(payment.getAmountInCents(), payment.getWithdrawPercent()));
        this.wechatPayService.tryRefund(wechatOrder, "撤销付费问题。");
        this.paymentService.updateAll(Collections.singletonList(payment));
    }

    /**
     * Activates the pending payment for settling.
     *
     * @param feedId Feed id.
     * @param userId User id.
     */
    @Transactional
    public void activatePayment(String feedId, String userId) {
        WechatOrder wechatOrder = this.wechatOrderService.retrieveByQuestionId(feedId);
        Payment payment = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId()).stream().findFirst().orElse(null);

        if (Objects.isNull(payment) || PaymentState.PENDING.getValue() != payment.getState()) {
            log.warn("Failed to activate payment for feed {}", feedId);
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_WECHAT_ORDER_PLACE_ERROR);
        }

        if (!payment.getUserId().equals(userId)) {
            log.warn("Failed to activate payment {}: FORBIDDEN", payment.getId());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_FORBIDDEN);
        }

        payment.setState(PaymentState.OPEN.getValue());

        this.paymentService.updateAll(Collections.singletonList(payment));
    }

    public FeedMessage retrieveFeedById(String id, String userId) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.channel);

        RetrieveFeedByIdRequest request = RetrieveFeedByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .setIncludeDeleted(false)
                .build();

        FeedResponse feedResponse;
        try {
            feedResponse = stub.retrieveFeedById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(feedResponse)
                || !feedResponse.hasStatus()) {
            log.error(Objects.isNull(feedResponse) ? "retrieve feed returned null." : feedResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != feedResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(feedResponse.getStatus());
        }

        return feedResponse.getFeed();
    }

    /**
     * Updates the question feed to visible.
     *
     * @param feedId Feed id.
     */
    private void updatePaidQuestion(String feedId) {
        FeedServiceGrpc.FeedServiceBlockingStub stub = FeedServiceGrpc.newBlockingStub(this.channel);

        UpdateFeedPaidByIdRequest request = UpdateFeedPaidByIdRequest.newBuilder()
                .setId(feedId)
                .build();

        try {
            stub.updateFeedPaidById(request);
        } catch (Exception e) {
            log.error("call comfirm paid question failure! feed id: {}, exception: {}", feedId, e.getMessage());
        }
    }

    /**
     * Calculates the amount after withdraw ratio.
     *
     * @param amount Amount.
     * @param ratio  Ratio.
     * @return Final amount.
     */
    private Long calculateAmount(Long amount, int ratio) {
        assert amount > 0;
        return amount * ratio / 100L;
    }

}
