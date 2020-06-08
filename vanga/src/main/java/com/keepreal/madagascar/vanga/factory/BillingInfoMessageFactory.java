package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.vanga.BillingInfoMessage;
import com.keepreal.madagascar.vanga.model.BillingInfo;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the billing info message factory.
 */
@Component
public class BillingInfoMessageFactory {

    /**
     * Converts {@link BillingInfo} into {@link BillingInfoMessage}.
     *
     * @param billingInfo {@link BillingInfo}.
     * @return {@link BillingInfoMessage}.
     */
    public BillingInfoMessage valueOf(BillingInfo billingInfo) {
        if (Objects.isNull(billingInfo)) {
            return null;
        }

        return BillingInfoMessage.newBuilder()
                .setId(billingInfo.getId())
                .setUserId(billingInfo.getUserId())
                .setAccountNumber(billingInfo.getAccountNumber())
                .setIdNumber(billingInfo.getIdNumber())
                .setMobile(billingInfo.getMobile())
                .setName(billingInfo.getName())
                .setIsVerified(billingInfo.getVerified())
                .build();
    }

}
