package com.keepreal.madagascar.vanga.repository;

import com.keepreal.madagascar.vanga.model.SubscribeMembership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SubscribeMembershipRepository extends JpaRepository<SubscribeMembership, String> {

    @Query(value = "SELECT COUNT(*) FROM subscribe_membership WHERE island_id = ?1 AND is_deleted = FALSE AND expire_time > ?2", nativeQuery = true)
    Integer getMemberCountByIslandId(String islandId, long current_time);

    @Query(value = "SELECT COUNT(*) FROM subscribe_membership WHERE  membership_id = ?1 AND is_deleted = FALSE AND expire_time > ?2", nativeQuery = true)
    Integer getMemberCountByMembershipId(String membershipId, long current_time);

    SubscribeMembership findByUserIdAndMembershipIdAndDeletedIsFalse(String userId, String membershipId);

    @Query(value = "SELECT membership_id FROM subscribe_membership WHERE user_id = ?1 AND island_id = ?2 AND is_deleted = FALSE AND expire_time > ?3", nativeQuery = true)
    List<String> getMembershipIdListByUserIdAndIslandId(String userId, String islandId, long current_time);

    @Query(value = "SELECT membership_id FROM subscribe_membership WHERE user_id = ?1 AND is_deleted = FALSE AND expire_time > ?2", nativeQuery = true)
    List<String> getMembershipIdListByUserId(String userId, long current_time);

    Page<SubscribeMembership> findAllByUserId(String userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "UPDATE subscribe_membership SET user_id = ?1 WHERE user_id = ?2", nativeQuery = true)
    void mergeUserSubscribeMembership(String wechatUserId, String webMobileUserId);

}
