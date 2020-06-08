package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.MembershipSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the membership sku repository.
 */
@Repository
public interface MembershipSkuRepository extends JpaRepository<MembershipSku, String> {

    List<MembershipSku> findAllByMembershipIdAndActiveIsTrueAndDeletedIsFalse(String membershipId);

    Boolean existsByMembershipIdAndDeletedIsFalse(String membershipId);

}
