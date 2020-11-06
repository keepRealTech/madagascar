package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.IncomeSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeSupportRepository extends JpaRepository<IncomeSupport, String> {

    Page<IncomeSupport> findIncomeSupportsByUserIdAndDeletedIsFalseOrderByCentsDesc(String userId, Pageable pageable);

    IncomeSupport findIncomeSupportByUserIdAndSupporterIdAndDeletedIsFalse(String userId, String supporterId);
}
