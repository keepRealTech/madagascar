package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.vanga.UserPaymentMessage;
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
        if (Objects.isNull(payment)) {
            return null;
        }

        return UserPaymentMessage.newBuilder()
                .setId(payment.getId())
                .setIslandId(membershipSku.getIslandId())
                .setUserId(payment.getUserId())
                .setPayeeId(payment.getPayeeId())
                .setMembershipSku(this.skuMessageFactory.valueOf(membershipSku))
                .setCreatedAt(payment.getCreatedTime())
                .build();
    }

}
