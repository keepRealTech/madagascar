package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.ChatgroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the chat group member repository.
 */
@Repository
public interface ChatgroupMemberRepository extends JpaRepository<ChatgroupMember, String> {

    ChatgroupMember findByGroupIdAndUserIdAndDeletedIsFalse(String groupId, String userId);

}
