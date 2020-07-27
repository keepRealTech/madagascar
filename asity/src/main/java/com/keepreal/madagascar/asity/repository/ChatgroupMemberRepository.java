package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.ChatgroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Represents the chat group member repository.
 */
@Repository
public interface ChatgroupMemberRepository extends JpaRepository<ChatgroupMember, String> {

    ChatgroupMember findByGroupIdAndUserIdAndDeletedIsFalse(String groupId, String userId);

    List<ChatgroupMember> findAllByIslandIdAndUserIdAndDeletedIsFalse(String islandId, String userId);

    List<ChatgroupMember> findAllByGroupIdInAndUserIdAndDeletedIsFalse(List<String> groupIds, String userId);

    List<ChatgroupMember> findAllByUserIdAndDeletedIsFalse(String userId);

    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM chat_group_member WHERE island_id = ?1 AND is_deleted = FALSE", nativeQuery = true)
    Long countDistinctUserIdByIslandIdAndDeletedIsFalse(String islandId);

    @Query(value = "SELECT DISTINCT user_id FROM chat_group_member WHERE island_id = ?1 AND is_deleted = FALSE ORDER BY created_time DESC LIMIT 4", nativeQuery = true)
    List<String> selectTop4DistinctUserIdsByIslandIdAndDeletedIsFalseOrderByCreatedTime(String islandId);

    @Query(value = "SELECT user_id FROM chat_group_member WHERE group_id = ?1 AND is_deleted = FALSE ORDER BY created_time ASC",
           countQuery = "SELECT count(user_id) FROM chat_group_member WHERE group_id = ?1 AND is_deleted = FALSE",
           nativeQuery = true)
    Page<String> selectUserIdsByGroupIdAndDeletedIsFalse(String groupId, Pageable pageable);

}
