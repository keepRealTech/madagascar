package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
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

    @Transactional
    public void supportWithWechatOrder(WechatOrder wechatOrder) {
        if (Objects.isNull(wechatOrder) || WechatOrderState.SUCCESS.getValue() != wechatOrder.getState()) {
            return;
        }

        List<Payment> paymentList = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

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
