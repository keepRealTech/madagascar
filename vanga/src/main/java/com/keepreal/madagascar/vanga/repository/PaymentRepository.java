package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the payment repository.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    List<Payment> findAllByTypeAndCreatedTimeAfterAndDeletedIsFalse(Integer type, Long timestampAfter);

    List<Payment> findAllByOrderIdAndDeletedIsFalse(String orderId);

    Payment findTopByTradeNumAndTypeAndDeletedIsFalse(String tradeNum, Integer type);

}
