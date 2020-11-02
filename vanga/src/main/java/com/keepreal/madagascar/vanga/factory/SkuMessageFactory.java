package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.ShellSkuMessage;
import com.keepreal.madagascar.vanga.SponsorSkuMessage;
import com.keepreal.madagascar.vanga.SupportSkuMessage;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.model.SponsorSku;
import com.keepreal.madagascar.vanga.model.SupportSku;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the sku message factory.
 */
@Component
public class SkuMessageFactory {

    /**
     * Converts {@link ShellSku} into {@link ShellSkuMessage}.
     *
     * @param shellSku {@link ShellSku}.
     * @return {@link ShellSkuMessage}.
     */
    public ShellSkuMessage valueOf(ShellSku shellSku) {
        if (Objects.isNull(shellSku)) {
            return null;
        }

        return ShellSkuMessage.newBuilder()
                .setId(shellSku.getId())
                .setAppleSkuId(shellSku.getAppleSkuId())
                .setDescription(shellSku.getDescription())
                .setIsDefault(shellSku.getDefaultSku())
                .setPriceInCents(shellSku.getPriceInCents())
                .setShells(shellSku.getShells())
                .build();
    }

    /**
     * Converts {@link MembershipSku} into {@link MembershipSkuMessage}.
     *
     * @param membershipSku {@link MembershipSku}.
     * @return {@link MembershipSkuMessage}.
     */
    public MembershipSkuMessage valueOf(MembershipSku membershipSku) {
        if (Objects.isNull(membershipSku)) {
            return null;
        }

        return MembershipSkuMessage.newBuilder()
                .setId(membershipSku.getId())
                .setAppleSkuId(membershipSku.getAppleSkuId())
                .setDescription(membershipSku.getDescription())
                .setIsDefault(membershipSku.getDefaultSku())
                .setPriceInCents(membershipSku.getPriceInCents())
                .setPriceInShells(membershipSku.getPriceInShells())
                .setMembershipId(membershipSku.getMembershipId())
                .setTimeInMonths(membershipSku.getTimeInMonths())
                .setPermanent(membershipSku.getPermanent())
                .build();
    }

    public SupportSkuMessage valueOf(SupportSku supportSku) {
        if (Objects.isNull(supportSku)) {
            return null;
        }
        return SupportSkuMessage.newBuilder()
                .setId(supportSku.getId())
                .setPriceInCents(supportSku.getPriceInCents())
                .setPriceInShells(supportSku.getShells())
                .setDefaulted(supportSku.getDefaultSku())
                .build();
    }

    /**
     * Converts {@link SponsorSku} into {@link SponsorSkuMessage}.
     *
     * @param sponsorSku {@link SponsorSku}.
     * @return {@link SponsorSkuMessage}.
     */
    public SponsorSkuMessage valueOf(SponsorSku sponsorSku) {
        if (Objects.isNull(sponsorSku)) {
            return null;
        }

        return SponsorSkuMessage.newBuilder()
                .setId(sponsorSku.getId())
                .setSponsorId(sponsorSku.getSponsorId())
                .setIslandId(sponsorSku.getIslandId())
                .setHostId(sponsorSku.getHostId())
                .setGiftId(sponsorSku.getGiftId())
                .setQuantity(sponsorSku.getQuantity())
                .setPriceInCents(sponsorSku.getPriceInCents())
                .setIsCustom(sponsorSku.getCustomSku())
                .setDefaultSku(sponsorSku.getDefaultSku())
                .build();
    }

}
