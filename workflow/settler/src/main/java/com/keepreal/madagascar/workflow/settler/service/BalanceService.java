package com.keepreal.madagascar.workflow.settler.service;

import com.keepreal.madagascar.workflow.settler.model.Balance;
import com.keepreal.madagascar.workflow.settler.repository.BalanceRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Represents the balance service.
 */
@Service
public class BalanceService {

    private final BalanceRepository balanceRepository;

    /**
     * Constructs the balance service.
     *
     * @param balanceRepository {@link BalanceService}.
     */
    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    /**
     * Retrieves or creates the balance for a given user id.
     *
     * @param userId User id;
     * @return {@link Balance}.
     */
    @Transactional
    public Balance retrieveByUserId(String userId) {
        return this.balanceRepository.findTopByUserIdAndDeletedIsFalse(userId);
    }

    /**
     * Updates balance.
     *
     * @param balance {@link Balance}.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance updateBalance(Balance balance) {
        return this.balanceRepository.save(balance);
    }

    /**
     * Adds cents to the balance.
     *
     * @param balance       {@link Balance}.
     * @param amountInCents Amount to adds on.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance addOnCents(Balance balance, Long amountInCents) {
        balance = this.balanceRepository.findByIdAndDeletedIsFalse(balance.getId());
        balance.setBalanceInCents(balance.getBalanceEligibleInCents() + amountInCents);

        return this.updateBalance(balance);
    }

}
