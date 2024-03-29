package com.keepreal.madagascar.asity.repository;

import com.keepreal.madagascar.asity.model.ChatgroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Represents the chatgroup membership 
 */
@Repository
public interface ChatgroupMembershipRepository extends JpaRepository<ChatgroupMembership, String> {

    @Transactional
    void deleteAllByMembershipId(String membershipId);

    Integer countByMembershipIdIn(List<String> membershipIds);
}
