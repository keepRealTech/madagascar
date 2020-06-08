package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.ShellSkuMessage;
import com.keepreal.madagascar.vanga.model.Balance;
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
                .setDescription(shellSku.getDescription())
                .setIsDefault(shellSku.getDefaultSku())
                .setPriceInCents(shellSku.getPriceInCents())
                .setShells(shellSku.getShells())
                .build();
    }

}
