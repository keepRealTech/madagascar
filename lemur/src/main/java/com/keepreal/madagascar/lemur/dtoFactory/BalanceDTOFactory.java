package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.vanga.BalanceMessage;
import org.springframework.stereotype.Component;
import swagger.model.BalanceDTO;

import java.util.Objects;

/**
 * Represents the balance dto factory.
 */
@Component
public class BalanceDTOFactory {

    /**
     * Converts the {@link BalanceMessage} to {@link BalanceDTO}.
     *
     * @param balance {@link BalanceMessage}.
     * @return {@link BalanceDTO}.
     */
    public BalanceDTO valueOf(BalanceMessage balance) {
        if (Objects.isNull(balance)) {
            return null;
        }

        BalanceDTO balanceDTO = new BalanceDTO();
        balanceDTO.setId(balance.getId());
        balanceDTO.setUserId(balance.getUserId());
        balanceDTO.setBalanceEligibleInCents(balance.getBalanceEligibleInCents());
        balanceDTO.setBalanceInCents(balance.getBalanceInCents());
        balanceDTO.setBalanceInShells(balance.getBalanceInShells());

        return balanceDTO;
    }

}
