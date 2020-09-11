package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.Order;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.OrderState;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
public class SupportService {

    private final PaymentService paymentService;
    private final BalanceService balanceService;
    private final NotificationEventProducerService notificationEventProducerService;

    public SupportService(PaymentService paymentService,
                          BalanceService balanceService,
                          NotificationEventProducerService notificationEventProducerService) {
        this.paymentService = paymentService;
        this.balanceService = balanceService;
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
        payment.setState(PaymentState.OPEN.getValue());

        Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(payment.getPayeeId());
        this.balanceService.addOnCents(hostBalance, this.calculateAmount(payment.getAmountInCents(), hostBalance.getWithdrawPercent()));
        this.paymentService.updateAll(paymentList);
        this.sendAsyncMessage(payment.getUserId(), payment.getPayeeId(), payment.getAmountInCents());
    }

    private void sendAsyncMessage(String userId, String payeeId, Long priceInCents) {
        this.notificationEventProducerService.produceNewSupportNotificationEventAsync(userId, payeeId, priceInCents);
    }

    private Long calculateAmount(Long amount, int ratio) {
        assert amount > 0;
        return amount * ratio / 100L;
    }
}
