package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderState;
import org.redisson.client.RedisClient;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;

/**
 * Represents the shell service.
 */
@Service
public class ShellService {

    private final BalanceService balanceService;
    private final PaymentService paymentService;
    private final IOSOrderService iosOrderService;
    private final RedisClient redisClient;

    /**
     * Constructs the shell service.
     *
     * @param balanceService  {@link BalanceService}.
     * @param paymentService  {@link PaymentService}.
     * @param iosOrderService {@link IOSOrderService}.
     * @param redisClient     {@link RedisClient}.
     */
    public ShellService(BalanceService balanceService,
                        PaymentService paymentService,
                        IOSOrderService iosOrderService,
                        RedisClient redisClient) {
        this.balanceService = balanceService;
        this.paymentService = paymentService;
        this.iosOrderService = iosOrderService;
        this.redisClient = redisClient;
    }

    /**
     * Verifies the ios receipt and buy shell for user.
     *
     * @param userId  User id.
     * @param receipt Receipt content.
     * @param sku     {@link ShellSku}.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance buyShell(String userId, String receipt, ShellSku sku) {
        Balance userBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(userId);

        String transactionId = this.iosOrderService.verify(receipt, sku);
        this.paymentService.createIOSBuyShellPayments(userId, sku, transactionId);
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
        if (Objects.isNull(wechatOrder) || WechatOrderState.SUCCESS.getValue() != wechatOrder.getState()) {
            return;
        }

        Payment payment = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

        Balance userBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(userId);

        String transactionId = this.iosOrderService.verify(receipt, sku);
        this.paymentService.createIOSBuyShellPayments(userId, sku, transactionId);
        return this.balanceService.addOnShells(userBalance, sku.getShells());
    }

}



         List<Payment> paymentList = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

        if (paymentList.stream().allMatch(payment -> PaymentState.OPEN.getValue() == payment.getState())) {
        return;
        }

        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("member-wechat-%s", wechatOrder.getUserId()))) {
        List<Payment> innerPaymentList = this.paymentService.retrievePaymentsByOrderId(wechatOrder.getId());

        MembershipSku sku = this.membershipSkuService.retrieveMembershipSkuById(wechatOrder.getMemberShipSkuId());

        if (innerPaymentList.isEmpty()) {
        innerPaymentList = this.paymentService.createNewWechatPayments(wechatOrder, sku);
        } else if (innerPaymentList.stream().allMatch(payment -> PaymentState.OPEN.getValue() == payment.getState())) {
        return;
        }

        Balance hostBalance = this.balanceService.retrieveOrCreateBalanceIfNotExistsByUserId(sku.getHostId());

        SubscribeMembership currentSubscribeMembership = this.subscriptionMemberRepository.findByUserIdAndMembershipIdAndDeletedIsFalse(
        wechatOrder.getUserId(), sku.getMembershipId());

        Instant instant = Objects.isNull(currentSubscribeMembership) ?
        Instant.now() : Instant.ofEpochMilli(currentSubscribeMembership.getExpireTime());
        ZonedDateTime currentExpireTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());

        List<Payment> finalInnerPaymentList = innerPaymentList;
        IntStream.range(0, innerPaymentList.size())
        .forEach(i -> {
        finalInnerPaymentList.get(i).setWithdrawPercent(hostBalance.getWithdrawPercent());
        finalInnerPaymentList.get(i).setState(PaymentState.OPEN.getValue());
        finalInnerPaymentList.get(i).setValidAfter(currentExpireTime
        .plusMonths((i + 1) * SubscribeMembershipService.PAYMENT_SETTLE_IN_MONTH)
        .toInstant().toEpochMilli());
        });

        this.balanceService.addOnCents(hostBalance, this.calculateAmount(sku.getPriceInCents(), hostBalance.getWithdrawPercent()));
        this.paymentService.updateAll(innerPaymentList);
        this.createOrRenewSubscriptionMember(wechatOrder.getUserId(), sku, currentSubscribeMembership, currentExpireTime);
        }