package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.common.PaymentState;
import com.keepreal.madagascar.common.UserPaymentType;
import com.keepreal.madagascar.vanga.UserPaymentMessage;
import com.keepreal.madagascar.vanga.UserWithdrawMessage;
import com.keepreal.madagascar.vanga.model.AlipayOrder;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import com.keepreal.madagascar.vanga.model.PaymentType;
import com.keepreal.madagascar.vanga.model.SponsorSku;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.service.AlipayOrderService;
import com.keepreal.madagascar.vanga.service.SkuService;
import com.keepreal.madagascar.vanga.service.WechatOrderService;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the payment message factory.
 */
@Component
public class PaymentMessageFactory {

    private final SkuMessageFactory skuMessageFactory;
    private final AlipayOrderService alipayOrderService;
    private final WechatOrderService wechatOrderService;
    private final SkuService skuService;

    /**
     * Constructs the payment message factory.
     *
     * @param skuMessageFactory  {@link SkuMessageFactory}.
     * @param alipayOrderService
     * @param wechatOrderService
     * @param skuService
     */
    public PaymentMessageFactory(SkuMessageFactory skuMessageFactory,
                                 AlipayOrderService alipayOrderService,
                                 WechatOrderService wechatOrderService,
                                 SkuService skuService) {
        this.skuMessageFactory = skuMessageFactory;
        this.alipayOrderService = alipayOrderService;
        this.wechatOrderService = wechatOrderService;
        this.skuService = skuService;
    }

    /**
     * Builds the {@link UserPaymentMessage}.
     *
     * @param payment       {@link Payment}.
     * @param membershipSku {@link MembershipSku}.
     * @return {@link UserPaymentMessage}.
     */
    public UserPaymentMessage valueOf(Payment payment, MembershipSku membershipSku) {
        if (Objects.isNull(payment)) {
            return null;
        }

        UserPaymentMessage.Builder paymentBuilder = UserPaymentMessage.newBuilder()
                .setId(payment.getId())
                .setUserId(payment.getUserId())
                .setPayeeId(payment.getPayeeId())
                .setCreatedAt(payment.getCreatedTime());

        if (Objects.nonNull(membershipSku)) {
            paymentBuilder.setType(UserPaymentType.PAYMENT_TYPE_MEMBERSHIP)
                    .setExpiresAt(payment.getValidAfter())
                    .setIslandId(membershipSku.getIslandId())
                    .setMembershipSku(this.skuMessageFactory.valueOf(membershipSku))
                    .setPriceInCents(membershipSku.getPriceInCents());
        } else if (PaymentType.SUPPORT.getValue() == payment.getType()) {
            paymentBuilder.setType(UserPaymentType.PAYMENT_TYPE_SUPPORT)
                    .setPriceInCents(payment.getAmountInCents());
            String orderId = payment.getOrderId();
            AlipayOrder alipayOrder = alipayOrderService.retrieveById(orderId);
            SponsorSku sponsorSku = null;
            if (alipayOrder != null) {
                sponsorSku = this.skuService.retrieveSponsorSkuById(alipayOrder.getPropertyId());
            } else {
                WechatOrder wechatOrder = wechatOrderService.retrieveById(orderId);
                if (wechatOrder != null) {
                    sponsorSku = this.skuService.retrieveSponsorSkuById(wechatOrder.getPropertyId());
                }
            }
            if (sponsorSku != null) {
                paymentBuilder.setSponsorGiftId(sponsorSku.getGiftId());
                paymentBuilder.setGiftCount(sponsorSku.getQuantity().intValue());
            }

        } else {
            paymentBuilder.setType(UserPaymentType.PAYMENT_TYPE_FEED)
                    .setPriceInCents(payment.getAmountInCents());
        }

        return paymentBuilder.build();
    }

    /**
     * Builds the {@link UserWithdrawMessage}.
     *
     * @param payment {@link Payment}.
     * @return {@link UserWithdrawMessage}.
     */
    public UserWithdrawMessage withdrawValueOf(Payment payment) {
        if (Objects.isNull(payment)) {
            return null;
        }

        return UserWithdrawMessage.newBuilder()
                .setId(payment.getId())
                .setAmountInCents(payment.getAmountInCents())
                .setCreatedAt(payment.getCreatedTime())
                .setState(this.convertState(payment.getState()))
                .build();
    }

    /**
     * Converts the payment state.
     *
     * @param state {@link com.keepreal.madagascar.vanga.model.PaymentState}.
     * @return {@link PaymentState}.
     */
    private PaymentState convertState(Integer state) {
        if (Objects.isNull(state)) {
            return PaymentState.UNRECOGNIZED;
        }

        switch (state) {
            case 2:
                return PaymentState.PAYMENT_STATE_OPEN;
            case 3:
                return PaymentState.PAYMENT_STATE_CLOSED;
            default:
                return PaymentState.UNRECOGNIZED;
        }
    }

}
