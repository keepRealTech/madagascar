package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.Balance;
import com.keepreal.madagascar.vanga.repository.BalanceRepository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Objects;

/**
 * Represents the balance service.
 */
@Component
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final LongIdGenerator idGenerator;
    private final SupportTargetService supportTargetService;

    /**
     * Constructs the balance service.
     *
     * @param balanceRepository {@link BalanceRepository}.
     * @param idGenerator       {@link LongIdGenerator}.
     * @param supportTargetService {@link SupportTargetService}
     */
    public BalanceService(BalanceRepository balanceRepository,
                          LongIdGenerator idGenerator,
                          SupportTargetService supportTargetService) {
        this.balanceRepository = balanceRepository;
        this.idGenerator = idGenerator;
        this.supportTargetService = supportTargetService;
    }

    /**
     * Retrieves or creates the balance for a given user id.
     *
     * @param userId User id;
     * @return {@link Balance}.
     */
    @Transactional
    public Balance retrieveOrCreateBalanceIfNotExistsByUserId(String userId) {
        Balance balance = this.balanceRepository.findTopByUserIdAndDeletedIsFalse(userId);
        if (Objects.nonNull(balance)) {
            return balance;
        }

        return this.createNewBalance(userId);
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
     * Creates a new balance for the given user id.
     *
     * @param userId User id.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance createNewBalance(String userId) {
        Balance balance = Balance.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .userId(userId)
                .build();

        return this.balanceRepository.save(balance);
    }

    /**
     * Withdraws from the balance.
     *
     * @param balance       {@link Balance}.
     * @param amountInCents Amount to withdraw.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance withdraw(Balance balance, Long amountInCents) {
        balance = this.balanceRepository.findByIdAndDeletedIsFalse(balance.getId());

        if (balance.getBalanceEligibleInCents() < amountInCents) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_BALANCE_WITHDRAW_LIMIT_ERROR);
        }

        balance.setBalanceEligibleInCents(balance.getBalanceEligibleInCents() - amountInCents);
        balance.setBalanceInCents(balance.getBalanceInCents() - amountInCents);

        return this.updateBalance(balance);
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
        balance.setBalanceInCents(balance.getBalanceInCents() + amountInCents);

        this.supportTargetService.UpdateSupportTargetIfExisted(balance.getUserId(), amountInCents);
        return this.updateBalance(balance);
    }

    /**
     * Subtracts cents from the balance.
     *
     * @param balance       {@link Balance}.
     * @param amountInCents Amount to subtract.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance subtractCents(Balance balance, Long amountInCents) {
        balance = this.balanceRepository.findByIdAndDeletedIsFalse(balance.getId());
        balance.setBalanceInCents(balance.getBalanceInCents() - amountInCents);

        return this.updateBalance(balance);
    }

    /**
     * Consumes shells from the balance.
     *
     * @param balance        {@link Balance}.
     * @param amountInShells Amount to consume.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance consumeShells(Balance balance, Long amountInShells) {
        balance = this.balanceRepository.findByIdAndDeletedIsFalse(balance.getId());

        if (balance.getBalanceInShells() < amountInShells) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_SHELL_INSUFFICIENT_ERROR);
        }
        balance.setBalanceInShells(balance.getBalanceInShells() - amountInShells);

        return this.updateBalance(balance);
    }

    /**
     * Adds on shells from the balance.
     *
     * @param balance        {@link Balance}.
     * @param amountInShells Amount to add on.
     * @return {@link Balance}.
     */
    @Transactional
    public Balance addOnShells(Balance balance, Long amountInShells) {
        balance = this.balanceRepository.findByIdAndDeletedIsFalse(balance.getId());
        balance.setBalanceInShells(balance.getBalanceInShells() + amountInShells);

        return this.updateBalance(balance);
    }

}
