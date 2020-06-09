package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.SubscribeMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscribeMembershipRepository extends JpaRepository<SubscribeMembership, String> {

    @Query(value = "SELECT COUNT(*) FROM subscribe_membership WHERE island_id = ?1 AND expire_time < ?2", nativeQuery = true)
    Integer getMemberCountByIslandId(String islandId, long deadline);

    @Query(value = "SELECT COUNT(*) FROM subscribe_membership WHERE  membership_id = ?1 AND expire_time < ?2", nativeQuery = true)
    Integer getMemberCountByMembershipId(String membershipId, long deadline);
}
