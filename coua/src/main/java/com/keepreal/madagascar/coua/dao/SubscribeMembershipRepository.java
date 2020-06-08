package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.SubscribeMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscribeMembershipRepository extends JpaRepository<SubscribeMembership, String> {

    @Query(value = "SELECT COUNT(*) FROM subscribe_membership WHERE island_id = ?1 AND expire_time < ?2", nativeQuery = true)
    Integer getMemberCountByIslandId(String islandId, long deadline);

    @Query(value = "SELECT COUNT(*) FROM subscribe_membership WHERE island_id = ?1 AND membership_id = ?2 AND expire_time < ?3", nativeQuery = true)
    Integer getMemberCountByMembershipId(String membershipId, long deadline);
}
