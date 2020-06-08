package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.vanga.BillingInfoMessage;
import org.springframework.stereotype.Component;
import swagger.model.BillingInfoDTO;

import java.util.Objects;

/**
 * Represents the billing info dto factory.
 */
@Component
public class BillingInfoDTOFactory {

    /**
     * Converts the {@link BillingInfoMessage} to {@link BillingInfoDTO}.
     *
     * @param billingInfo {@link BillingInfoMessage}.
     * @return {@link BillingInfoDTO}.
     */
    public BillingInfoDTO valueOf(BillingInfoMessage billingInfo) {
        if (Objects.isNull(billingInfo)) {
            return null;
        }

        BillingInfoDTO billingInfoDTO = new BillingInfoDTO();
        billingInfoDTO.setId(billingInfo.getId());
        billingInfoDTO.setAccount(billingInfo.getAccountNumber());
        billingInfoDTO.setIdentityNumber(billingInfo.getIdNumber());
        billingInfoDTO.setMobile(billingInfo.getMobile());
        billingInfoDTO.setName(billingInfo.getName());
        billingInfoDTO.setVerified(billingInfo.getIsVerified());
        billingInfoDTO.setUserId(billingInfo.getUserId());

        return billingInfoDTO;
    }

}
