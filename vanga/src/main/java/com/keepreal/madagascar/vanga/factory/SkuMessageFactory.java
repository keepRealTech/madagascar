package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.vanga.MembershipSkuMessage;
import com.keepreal.madagascar.vanga.ShellSkuMessage;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.ShellSku;
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
                .build();
    }

}
