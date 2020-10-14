package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.FeedCharge;
import com.keepreal.madagascar.vanga.model.Order;
import com.keepreal.madagascar.vanga.model.OrderState;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.repository.FeedChargeRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class FeedChargeService {

    private final PaymentService paymentService;
    private final BalanceService balanceService;
    private final FeedChargeRepository feedChargeRepository;
    private final LongIdGenerator idGenerator;
    private final NotificationEventProducerService notificationEventProducerService;

    public FeedChargeService(PaymentService paymentService,
                             BalanceService balanceService,
                             FeedChargeRepository feedChargeRepository,
                             LongIdGenerator idGenerator,
                             NotificationEventProducerService notificationEventProducerService) {
        this.paymentService = paymentService;
        this.balanceService = balanceService;
        this.feedChargeRepository = feedChargeRepository;
        this.idGenerator = idGenerator;
        this.notificationEventProducerService = notificationEventProducerService;
    }

    @Transactional
    public void feedChargeWithWechatOrder(Order order) {
        if (Objects.isNull(order) || OrderState.SUCCESS.getValue() != order.getState()) {
            return;
        }

        List<Payment> paymentList = this.paymentService.retrievePaymentsByOrderId(order.getId());

        if (paymentList.stream().allMatch(payment -> PaymentState.DRAFTED.getValue() != payment.getState())) {
            return;
        }

        ZonedDateTime currentTimestamp = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        Payment payment = paymentList.get(0);
        payment.setState(PaymentState.OPEN.getValue());
        payment.setValidAfter(currentTimestamp
                .plusMonths(SubscribeMembershipService.PAYMENT_SETTLE_IN_MONTH)
                .toInstant().toEpochMilli());

        Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(payment.getPayeeId());
        this.balanceService.addOnCents(hostBalance, this.calculateAmount(payment.getAmountInCents(), hostBalance.getWithdrawPercent()));
        this.paymentService.updateAll(paymentList);
        this.saveFeedCharge(order.getUserId(), order.getPropertyId());

        this.notificationEventProducerService.produceNewFeedPaymentNotificationEventAsync(payment.getUserId(), payment.getPayeeId(), order.getPropertyId());
    }

    public FeedCharge findFeedCharge(String userId, String feedId) {
        return this.feedChargeRepository.findFeedChargeByUserIdAndFeedIdAndDeletedIsFalse(userId, feedId);
    }

    private void saveFeedCharge(String userId, String feedId) {
        FeedCharge feedCharge = this.feedChargeRepository.findFeedChargeByUserIdAndFeedIdAndDeletedIsFalse(userId, feedId);
        if (feedCharge == null) {
            feedCharge = new FeedCharge();
            feedCharge.setId(String.valueOf(this.idGenerator.nextId()));
            feedCharge.setUserId(userId);
            feedCharge.setFeedId(feedId);
            this.feedChargeRepository.save(feedCharge);
        }
    }

    private Long calculateAmount(Long amount, int ratio) {
        assert amount > 0;
        return amount * ratio / 100L;
    }
}
