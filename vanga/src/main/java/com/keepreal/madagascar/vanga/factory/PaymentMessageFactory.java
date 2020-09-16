package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.common.PaymentState;
import com.keepreal.madagascar.vanga.UserPaymentMessage;
import com.keepreal.madagascar.vanga.UserWithdrawMessage;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.Payment;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the payment message factory.
 */
@Component
public class PaymentMessageFactory {

    private final SkuMessageFactory skuMessageFactory;

    /**
     * Constructs the payment message factory.
     *
     * @param skuMessageFactory {@link SkuMessageFactory}.
     */
    public PaymentMessageFactory(SkuMessageFactory skuMessageFactory) {
        this.skuMessageFactory = skuMessageFactory;
    }

    /**
     * Builds the {@link UserPaymentMessage}.
     *
     * @param payment       {@link Payment}.
     * @param membershipSku {@link MembershipSku}.
     * @return {@link UserPaymentMessage}.
     */
    public UserPaymentMessage valueOf(Payment payment, MembershipSku membershipSku) {
        if (Objects.isNull(payment) || Objects.isNull(membershipSku)) {
            return null;
        }

        return UserPaymentMessage.newBuilder()
                .setId(payment.getId())
                .setIslandId(membershipSku.getIslandId())
                .setUserId(payment.getUserId())
                .setPayeeId(payment.getPayeeId())
                .setMembershipSku(this.skuMessageFactory.valueOf(membershipSku))
                .setExpiresAt(payment.getValidAfter())
                .setCreatedAt(payment.getCreatedTime())
                .build();
    }

    /**
     * Builds the {@link UserWithdrawMessage}.
     *
     * @param payment       {@link Payment}.
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
