package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.MembershipInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface MembershipInfoRepository extends JpaRepository<MembershipInfo, String> {

    MembershipInfo findMembershipInfoByIdAndActiveIsTrueAndDeletedIsFalse(String id);

    List<MembershipInfo> findMembershipInfosByIslandIdAndDeletedIsFalseOrderByTopDescPricePerMonthAsc(String islandId);

    List<MembershipInfo> findMembershipInfosByIslandIdAndActiveIsTrueAndDeletedIsFalseOrderByTopDescPricePerMonthAsc(String islandId);

    List<MembershipInfo> findMembershipInfosByIslandIdInAndActiveIsTrueAndDeletedIsFalseOrderByTopDescPricePerMonthAsc(List<String> islandIds);

    @Query(value = "SELECT color_type FROM membership WHERE island_id = ?1 AND is_active = TRUE AND is_deleted = FALSE", nativeQuery = true)
    List<Integer> getColorTypeListByIslandId(String islandId);

    MembershipInfo findMembershipInfoByIslandIdAndTopIsTrueAndActiveIsTrueAndDeletedIsFalse(String islandId);

    Integer countByIslandIdAndDeletedIsFalse(String islandId);

    List<MembershipInfo> findAllByIdInAndDeletedIsFalse(Iterable<String> ids);

    @Modifying
    @Transactional
    @Query(value = "UPDATE membership SET member_count = member_count + 1 WHERE id = ?1", nativeQuery = true)
    void addMemberCountByMembershipId(String membershipId);
}
