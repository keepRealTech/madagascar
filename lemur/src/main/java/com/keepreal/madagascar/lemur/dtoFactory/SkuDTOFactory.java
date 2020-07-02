package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.ShellSkuMessage;
import org.springframework.stereotype.Service;
import swagger.model.IOSShellSkuDTO;
import swagger.model.MembershipSkuDTO;

import java.util.Objects;

/**
 * Represents the sku dto factory.
 */
@Service
public class SkuDTOFactory {

    /**
     * Converts {@link ShellSkuMessage} into {@link IOSShellSkuDTO}.
     *
     * @param shellSku {@link ShellSkuMessage}.
     * @return {@link IOSShellSkuDTO}.
     */
    public IOSShellSkuDTO iosValueOf(ShellSkuMessage shellSku) {
        if (Objects.isNull(shellSku)) {
            return null;
        }

        IOSShellSkuDTO shellSkuDTO = new IOSShellSkuDTO();
        shellSkuDTO.setId(shellSku.getId());
        shellSkuDTO.setAppleSkuId(shellSku.getAppleSkuId());
        shellSkuDTO.setDescription(shellSku.getDescription());
        shellSkuDTO.setIsDefault(shellSku.getIsDefault());
        shellSkuDTO.setShells(shellSku.getShells());
        shellSkuDTO.setPriceInCents(shellSku.getPriceInCents());

        return shellSkuDTO;
    }

    /**
     * Converts {@link MembershipSkuMessage} into {@link MembershipSkuDTO}.
     *
     * @param membershipSku {@link MembershipSkuMessage}.
     * @return {@link MembershipSkuDTO}.
     */
    public MembershipSkuDTO iosValueOf(MembershipSkuMessage membershipSku) {
        if (Objects.isNull(membershipSku)) {
            return null;
        }

        MembershipSkuDTO membershipSkuDTO = new MembershipSkuDTO();
        membershipSkuDTO.setId(membershipSku.getId());
        membershipSkuDTO.setDescription(membershipSku.getDescription());
        membershipSkuDTO.setIsDefault(membershipSku.getIsDefault());
        membershipSkuDTO.setPriceInCents(membershipSku.getPriceInCents());
        membershipSkuDTO.setPriceInShells(membershipSku.getPriceInShells());
        membershipSkuDTO.setMembershipId(membershipSku.getMembershipId());
        membershipSkuDTO.setTimeInMonths(membershipSku.getTimeInMonths());

        return membershipSkuDTO;
    }

}
