package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.ChatgroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Represents the chatgroup membership 
 */
@Repository
public interface ChatgroupMembershipRepository extends JpaRepository<ChatgroupMembership, String> {

    void deleteAllByMembershipId(String membershipId);

}
