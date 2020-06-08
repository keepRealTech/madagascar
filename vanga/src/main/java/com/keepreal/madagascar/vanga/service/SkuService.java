package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.repository.MembershipSkuRepository;
import com.keepreal.madagascar.vanga.repository.ShellSkuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Represents the sku service.
 */
@Service
public class SkuService {

    private final ShellSkuRepository shellSkuRepository;
    private final MembershipSkuRepository membershipSkuRepository;

    /**
     * Constructs the sku service.
     *
     * @param shellSkuRepository      {@link ShellSkuRepository}.
     * @param membershipSkuRepository {@link MembershipSkuRepository}.
     */
    public SkuService(ShellSkuRepository shellSkuRepository,
                      MembershipSkuRepository membershipSkuRepository) {
        this.shellSkuRepository = shellSkuRepository;
        this.membershipSkuRepository = membershipSkuRepository;
    }

    /**
     * Retrieves all active shell skus.
     *
     * @return {@link ShellSku}.
     */
    public List<ShellSku> retrieveShellSkusByActiveIsTrue() {
        return this.shellSkuRepository.findAllByActiveIsTrueAndDeletedIsFalse();
    }

    /**
     * Retrieves active membership skus for a given membership id.
     *
     * @param membershipId Membership id.
     * @return {@link MembershipSku}.
     */
    public List<MembershipSku> retrieveMembershipSkusByMembershipIdAndActiveIsTrue(String membershipId) {
        return this.membershipSkuRepository.findAllByMembershipIdAndActiveIsTrueAndDeletedIsFalse(membershipId);
    }

}
