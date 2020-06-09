package com.keepreal.madagascar.vanga.factory;

import com.keepreal.madagascar.vanga.BalanceMessage;
import com.keepreal.madagascar.vanga.model.Balance;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Represents the balance message factory.
 */
@Component
public class BalanceMessageFactory {

    /**
     * Converts {@link Balance} into {@link BalanceMessage}.
     *
     * @param balance {@link Balance}.
     * @return {@link BalanceMessage}.
     */
    public BalanceMessage valueOf(Balance balance) {
        if (Objects.isNull(balance)) {
            return null;
        }

        return BalanceMessage.newBuilder()
                .setId(balance.getId())
                .setUserId(balance.getUserId())
                .setBalanceInCents(balance.getBalanceInCents())
                .setBalanceInShells(balance.getBalanceInShells())
                .setBalanceEligibleInCents(balance.getBalanceEligibleInCents())
                .build();
    }

}
