package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the balance repository.
 */
@Repository
public interface BalanceRepository extends JpaRepository<Balance, String> {

    Balance findTopByUserIdAndDeletedIsFalse(String userId);

}
