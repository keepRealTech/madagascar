package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.ChatgroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
