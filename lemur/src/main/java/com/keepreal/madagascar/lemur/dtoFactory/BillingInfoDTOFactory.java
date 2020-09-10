package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.vanga.BillingInfoMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import swagger.model.BillingInfoDTO;
import swagger.model.BillingInfoDTOV11;

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
        billingInfoDTO.setIdFrontUrl(billingInfo.getIdFrontUrl());
        billingInfoDTO.setIdBackUrl(billingInfo.getIdBackUrl());

        return billingInfoDTO;
    }

    /**
     * Converts the {@link BillingInfoMessage} to {@link BillingInfoDTOV11}.
     *
     * @param billingInfo {@link BillingInfoMessage}.
     * @return {@link BillingInfoDTOV11}.
     */
    public BillingInfoDTOV11 v11ValueOf(BillingInfoMessage billingInfo) {
        if (Objects.isNull(billingInfo)) {
            return null;
        }

        String name = !StringUtils.isEmpty(billingInfo.getName()) ? billingInfo.getName().charAt(0) + this.paddingMask(billingInfo.getName()) : "";

        BillingInfoDTOV11 billingInfoDTO = new BillingInfoDTOV11();
        billingInfoDTO.setId(billingInfo.getId());
        billingInfoDTO.setMobile(billingInfo.getMobile());
        billingInfoDTO.setName(name);
        billingInfoDTO.setUserId(billingInfo.getUserId());
        billingInfoDTO.setAlipayAccount(billingInfo.getAliPayAccount());

        billingInfoDTO.setVerified(!StringUtils.isEmpty(billingInfo.getAliPayAccount())
                && !StringUtils.isEmpty(billingInfo.getName())
                && !StringUtils.isEmpty(billingInfo.getMobile()));

        return billingInfoDTO;
    }

    /**
     * Padding name with asterisks.
     *
     * @param name Name.
     * @return ******.
     */
    private String paddingMask(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length() - 1; i++) {
            sb.append("*");
        }
        return sb.toString();
    }

}
