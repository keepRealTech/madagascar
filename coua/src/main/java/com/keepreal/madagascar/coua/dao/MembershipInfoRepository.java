package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.MembershipInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipInfoRepository extends JpaRepository<MembershipInfo, String> {

    MembershipInfo findMembershipInfoByIdAndActivateIsTrueAndDeletedIsFalse(String id);

    List<MembershipInfo> findMembershipInfosByIslandIdAndActivateIsTrueAndDeletedIsFalse(String islandId);

    @Query(value = "SELECT color_type FROM membership WHERE island_id = ?1 AND is_activate = TRUE AND is_deleted = FALSE", nativeQuery = true)
    List<Integer> getColorTypeListByIslandId(String islandId);

    MembershipInfo findMembershipInfoByIslandIdAndTopIsTrue(String islandId);
}
