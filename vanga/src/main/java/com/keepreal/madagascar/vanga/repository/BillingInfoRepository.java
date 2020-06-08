package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.BillingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the billing info repository.
 */
@Repository
public interface BillingInfoRepository extends JpaRepository<BillingInfo, String> {

    BillingInfo findTopByUserIdAndDeletedIsFalse(String userId);

}
