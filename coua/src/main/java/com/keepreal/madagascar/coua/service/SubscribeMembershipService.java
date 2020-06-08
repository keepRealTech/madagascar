package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.coua.dao.SubscribeMembershipRepository;
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

    public Integer getMemberCountByIslandId(String islandId) {
        LocalDate localDate = LocalDate.now().plusDays(1L);
        long deadline = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return repository.getMemberCountByIslandId(islandId, deadline);
    }

    public Integer getMemberCountByMembershipId(String membershipId) {
        LocalDate localDate = LocalDate.now().plusDays(1L);
        long deadline = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return repository.getMemberCountByMembershipId(membershipId, deadline);
    }
}
