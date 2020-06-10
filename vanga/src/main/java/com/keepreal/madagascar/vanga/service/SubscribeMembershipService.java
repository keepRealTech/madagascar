package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.repository.SubscribeMembershipRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Represents the subscribe membership service.
 */
@Service
public class SubscribeMembershipService {

    private final SubscribeMembershipRepository repository;

    /**
     * Constructor the subscribe membership service.
     *
     * @param repository    {@link SubscribeMembershipRepository}.
     */
    public SubscribeMembershipService(SubscribeMembershipRepository repository) {
        this.repository = repository;
    }

    /**
     * retrieve the membership count by island id.
     *
     * @param islandId  island id.
     * @return  member count.
     */
    public Integer getMemberCountByIslandId(String islandId) {
        return repository.getMemberCountByIslandId(islandId, getDeadline());
    }

    /**
     * retrieve the membership count by membership id.
     *
     * @param membershipId  membership id.
     * @return  member count.
     */
    public Integer getMemberCountByMembershipId(String membershipId) {
        return repository.getMemberCountByMembershipId(membershipId, getDeadline());
    }

    /**
     * retrieve the membership id list by user id and island id.
     *
     * @param userId    user id.
     * @param islandId  island id.
     * @return  membership id list.
     */
    public List<String> getMembershipIdListByUserIdAndIslandId(String userId, String islandId) {
        return repository.getMembershipIdListByUserIdAndIslandId(userId, islandId, getDeadline());
    }

    private long getDeadline() {
        LocalDate localDate = LocalDate.now().plusDays(1L);
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
