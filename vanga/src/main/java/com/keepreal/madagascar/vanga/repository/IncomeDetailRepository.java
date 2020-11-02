package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.IncomeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncomeDetailRepository extends JpaRepository<IncomeDetail, String> {

    List<IncomeDetail> findIncomeDetailsByUserIdAndDeletedIsFalseOrderByMonthTimestampDesc(String userId);

    IncomeDetail findIncomeDetailByUserIdAndMonthTimestampAndDeletedIsFalse(String userId, Long monthTimestamp);
}
