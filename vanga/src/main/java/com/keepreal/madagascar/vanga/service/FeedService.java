package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.fossa.FeedServiceGrpc;
import com.keepreal.madagascar.fossa.UpdateFeedPaidByIdRequest;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.SubscribeMembership;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Represents the feed service.
 */
@Slf4j
@Service
public class FeedService {

    private final Channel channel;
    private final PaymentService paymentService;
    private final BalanceService balanceService;

    /**
     * Constructs the feed service.
     *  @param channel Managed channel for grpc traffic.
     * @param paymentService {@link PaymentService}.
     * @param balanceService    {@link BalanceService}.
     */
    public FeedService(@Qualifier("fossaChannel") Channel channel,
                       PaymentService paymentService,
                       BalanceService balanceService) {
        this.channel = channel;
        this.paymentService = paymentService;
        this.balanceService = balanceService;
    }

    /**
     * Confirms a pay question paid.
     *
     * @param wechatOrder {@link WechatOrder}.
     */
    @Transactional
    public void confirmQuestionPaid(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder) || WechatOrderState.SUCCESS.getValue() != wechatOrder.getState()) {
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

        this.balanceService.addOnCents(hostBalance, this.calculateAmount(payment.getAmountInCents(), hostBalance.getWithdrawPercent()));
        this.paymentService.updateAll(Collections.singletonList(payment));
        this.updatePaidQuestion(wechatOrder.getPropertyId());
    }

    /**
     * Updates the question feed to visible.
     *
     * @param feedId Feed id.
     */
    private void updatePaidQuestion(String feedId) {
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
