package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.repository.SubscribeMembershipRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

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
        LocalDate localDate = LocalDate.now().plusDays(1L);
        long deadline = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return repository.getMemberCountByIslandId(islandId, deadline);
    }

    /**
     * retrieve the membership count by membership id.
     *
     * @param membershipId  membership id.
     * @return  member count.
     */
    public Integer getMemberCountByMembershipId(String membershipId) {
        LocalDate localDate = LocalDate.now().plusDays(1L);
        long deadline = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return repository.getMemberCountByMembershipId(membershipId, deadline);
    }
}
