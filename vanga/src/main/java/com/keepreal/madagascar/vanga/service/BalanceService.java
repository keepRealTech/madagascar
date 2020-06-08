package com.keepreal.madagascar.vanga.service;

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

    /**
     * Constructs the balance service.
     *
     * @param balanceRepository {@link BalanceRepository}.
     * @param idGenerator       {@link LongIdGenerator}.
     */
    public BalanceService(BalanceRepository balanceRepository,
                          LongIdGenerator idGenerator) {
        this.balanceRepository = balanceRepository;
        this.idGenerator = idGenerator;
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
    private Balance createNewBalance(String userId) {
        Balance balance = Balance.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .userId(userId)
                .build();

        return this.balanceRepository.save(balance);
    }

}
