package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, String> {

}
