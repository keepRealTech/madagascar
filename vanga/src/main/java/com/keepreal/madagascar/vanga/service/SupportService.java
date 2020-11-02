package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.Order;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.OrderState;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SupportService {

    private final PaymentService paymentService;
    private final BalanceService balanceService;
    private final IncomeService incomeService;
    private final NotificationEventProducerService notificationEventProducerService;

    public SupportService(PaymentService paymentService,
                          BalanceService balanceService,
                          IncomeService incomeService,
                          NotificationEventProducerService notificationEventProducerService) {
        this.paymentService = paymentService;
        this.balanceService = balanceService;
        this.incomeService = incomeService;
        this.notificationEventProducerService = notificationEventProducerService;
    }

    /**
     * Supports with order.
     *
     * @param order {@link Order}.
     */
    @Transactional
    public void supportWithOrder(Order order) {
        if (Objects.isNull(order) || OrderState.SUCCESS.getValue() != order.getState()) {
            return;
        }

        List<Payment> paymentList = this.paymentService.retrievePaymentsByOrderId(order.getId());

        if (paymentList.stream().allMatch(payment -> PaymentState.DRAFTED.getValue() != payment.getState())) {
            return;
        }

        Payment payment = paymentList.get(0);
        Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(payment.getPayeeId());

        payment.setWithdrawPercent(hostBalance.getWithdrawPercent());
        payment.setState(PaymentState.OPEN.getValue());

        Instant instant = Instant.now();
        ZonedDateTime currentExpireTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

        payment.setValidAfter(currentExpireTime.plusMonths(1).toInstant().toEpochMilli());

        this.balanceService.addOnCents(hostBalance, this.calculateAmount(payment.getAmountInCents(), hostBalance.getWithdrawPercent()));
        this.paymentService.updateAll(paymentList);
        this.sendAsyncMessage(payment.getUserId(), payment.getPayeeId(), payment.getAmountInCents());

        this.incomeService.updateIncomeAll(payment.getPayeeId(), order.getUserId(), System.currentTimeMillis(), payment.getAmountInCents());
    }

    private void sendAsyncMessage(String userId, String payeeId, Long priceInCents) {
        this.notificationEventProducerService.produceNewSupportNotificationEventAsync(userId, payeeId, priceInCents);
    }

    private Long calculateAmount(Long amount, int ratio) {
        assert amount > 0;
        return amount * ratio / 100L;
    }
}
