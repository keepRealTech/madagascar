package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.ShellSkuMessage;
import com.keepreal.madagascar.vanga.SupportSkuMessage;
import org.springframework.stereotype.Service;
import swagger.model.IOSShellSkuDTO;
import swagger.model.MembershipSkuDTO;
import swagger.model.SponsorSkuDTO;
import swagger.model.SupportSkusDTO;
import swagger.model.WechatShellSkuDTO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the sku dto factory.
 */
@Service
public class SkuDTOFactory {

    private final static String CUSTOMIZED_SUPPORT_SKU_ID = "";

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
     * Converts {@link ShellSkuMessage} into {@link WechatShellSkuDTO}.
     *
     * @param shellSku {@link ShellSkuMessage}.
     * @return {@link WechatShellSkuDTO}.
     */
    public WechatShellSkuDTO wechatValueOf(ShellSkuMessage shellSku) {
        if (Objects.isNull(shellSku)) {
            return null;
        }

        WechatShellSkuDTO shellSkuDTO = new WechatShellSkuDTO();
        shellSkuDTO.setId(shellSku.getId());
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
    public MembershipSkuDTO valueOf(MembershipSkuMessage membershipSku) {
        if (Objects.isNull(membershipSku)) {
            return null;
        }

        MembershipSkuDTO membershipSkuDTO = new MembershipSkuDTO();
        membershipSkuDTO.setId(membershipSku.getId());
        membershipSkuDTO.setAppleSkuId(membershipSku.getAppleSkuId());
        membershipSkuDTO.setDescription(membershipSku.getDescription());
        membershipSkuDTO.setIsDefault(membershipSku.getIsDefault());
        membershipSkuDTO.setPriceInCents(membershipSku.getPriceInCents());
        membershipSkuDTO.setPriceInShells(membershipSku.getPriceInShells());
        membershipSkuDTO.setMembershipId(membershipSku.getMembershipId());
        membershipSkuDTO.setTimeInMonths(membershipSku.getTimeInMonths());

        return membershipSkuDTO;
    }

    public SupportSkusDTO valueOf(List<SupportSkuMessage> supportSkuMessages) {
        SupportSkusDTO dto = new SupportSkusDTO();
        dto.setSponsorSkus(supportSkuMessages.stream().map(this::sponsorValueOf).collect(Collectors.toList()));
        dto.setCustomizedSku(this.sponsorValueOf(CUSTOMIZED_SUPPORT_SKU_ID));
        return dto;
    }

    public SponsorSkuDTO sponsorValueOf(SupportSkuMessage supportSkuMessage) {
        SponsorSkuDTO dto = new SponsorSkuDTO();
        dto.setId(supportSkuMessage.getId());
        dto.setIsDefault(supportSkuMessage.getDefaulted());
        dto.setPriceInCents(supportSkuMessage.getPriceInCents());
        dto.setPriceInShells(supportSkuMessage.getPriceInShells());
        return dto;
    }

    private SponsorSkuDTO sponsorValueOf(String id) {
        SponsorSkuDTO dto = new SponsorSkuDTO();
        dto.setId(id);
        dto.setIsDefault(false);
        dto.setPriceInCents(0L);
        dto.setPriceInShells(0L);
        return dto;
    }

}
