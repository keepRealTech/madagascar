package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

}
