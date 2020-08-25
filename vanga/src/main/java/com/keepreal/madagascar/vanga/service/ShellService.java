package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.IosOrder;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentState;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import com.keepreal.madagascar.vanga.util.AutoRedisLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the shell service.
 */
@Service
public class ShellService {

    private final BalanceService balanceService;
    private final PaymentService paymentService;
    private final IOSOrderService iosOrderService;
    private final RedissonClient redissonClient;

    /**
     * Constructs the shell service.
     *
     * @param balanceService  {@link BalanceService}.
     * @param paymentService  {@link PaymentService}.
     * @param iosOrderService {@link IOSOrderService}.
     * @param redissonClient  {@link RedissonClient}.
     */
    public ShellService(BalanceService balanceService,
                        PaymentService paymentService,
                        IOSOrderService iosOrderService,
                        RedissonClient redissonClient) {
        this.balanceService = balanceService;
        this.paymentService = paymentService;
        this.iosOrderService = iosOrderService;
        this.redissonClient = redissonClient;
    }

    /**
     * Verifies the ios receipt and buy shell for user.
     *
     * @param userId  User id.
     * @param receipt Receipt content.
     * @param transactionId Transaction id.
     * @param sku     {@link ShellSku}.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance buyShell(String userId, String receipt, String transactionId, ShellSku sku) {
        Balance userBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(userId);

        IosOrder iosOrder = this.iosOrderService.verify(userId, receipt, sku.getDescription(), sku.getAppleSkuId(), sku.getId(), transactionId);
        this.paymentService.createIOSBuyShellPayments(userId, sku, iosOrder.getTransactionId(), iosOrder.getId());
        return this.balanceService.addOnShells(userBalance, sku.getShells());
    }

    /**
     * Verifies the ios receipt and buy shell for user.
     *
     * @param wechatOrder   {@link WechatOrder}.
     * @param sku           {@link ShellSku}.
     */
    @Transactional
    public void buyShellWithWechat(WechatOrder wechatOrder, ShellSku sku) {
        if (Objects.isNull(wechatOrder)
                || WechatOrderState.SUCCESS.getValue() != wechatOrder.getState()
                || Objects.isNull(sku)) {
            return;
        }

        List<Payment> paymentList = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

        if (!paymentList.isEmpty()
                && paymentList.stream().allMatch(payment -> PaymentState.CLOSED.getValue() == payment.getState())) {
            return;
        }

        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("shell-wechat-%s", wechatOrder.getUserId()))) {
            List<Payment> innerPaymentList = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

            if (innerPaymentList.isEmpty()) {
                innerPaymentList = Collections.singletonList(this.paymentService.createWechatBuyShellPayments(wechatOrder, sku));
            } else if (paymentList.stream().allMatch(payment -> PaymentState.CLOSED.getValue() == payment.getState())) {
                return;
            }

            Balance userBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(wechatOrder.getUserId());
            innerPaymentList = innerPaymentList.stream()
                    .peek(payment -> payment.setState(PaymentState.CLOSED.getValue()))
                    .collect(Collectors.toList());

            this.balanceService.addOnShells(userBalance, sku.getShells());
            this.paymentService.updateAll(innerPaymentList);
        }
    }

}