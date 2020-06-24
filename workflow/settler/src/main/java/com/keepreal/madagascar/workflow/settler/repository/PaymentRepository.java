package com.keepreal.madagascar.workflow.settler.repository;

import com.keepreal.madagascar.workflow.settler.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the payment repository.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findTop5000ByTypeInAndStateAndValidAfterBeforeAndDeletedIsFalseOrderByCreatedTime(List<Integer> types, Integer state, Long validAfter);

}