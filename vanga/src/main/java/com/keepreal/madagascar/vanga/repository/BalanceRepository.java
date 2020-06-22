package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

/**
 * Represents the balance repository.
 */
@Repository
public interface BalanceRepository extends JpaRepository<Balance, String> {

    Balance findTopByUserIdAndDeletedIsFalse(String userId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Balance findByIdAndDeletedIsFalse(String id);

}
