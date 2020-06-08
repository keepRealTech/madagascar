package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.repository.MembershipSkuRepository;
import com.keepreal.madagascar.vanga.repository.ShellSkuRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the sku service.
 */
@Service
public class SkuService {

    private final ShellSkuRepository shellSkuRepository;
    private final MembershipSkuRepository membershipSkuRepository;
    private final LongIdGenerator idGenerator;
    private final List<Integer> membershipPeriods = Stream.of(1, 3, 6, 12).collect(Collectors.toList());

    /**
     * Constructs the sku service.
     *
     * @param shellSkuRepository      {@link ShellSkuRepository}.
     * @param membershipSkuRepository {@link MembershipSkuRepository}.
     * @param idGenerator             {@link LongIdGenerator}.
     */
    public SkuService(ShellSkuRepository shellSkuRepository,
                      MembershipSkuRepository membershipSkuRepository,
                      LongIdGenerator idGenerator) {
        this.shellSkuRepository = shellSkuRepository;
        this.membershipSkuRepository = membershipSkuRepository;
        this.idGenerator = idGenerator;
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

    /**
     * Creates default membership skus if not exists.
     *
     * @param membershipId        Membership id.
     * @param costInCentsPerMonth Membership cost in cents per month.
     * @return {@link MembershipSku}.
     */
    @Transactional
    public List<MembershipSku> createDefaultMembershipSkusByMembershipIdAndCostPerMonth(String membershipId,
                                                                                        Long costInCentsPerMonth) {
        List<MembershipSku> membershipSkus = this.retrieveMembershipSkusByMembershipIdAndActiveIsTrue(membershipId);
        if (Objects.nonNull(membershipSkus) && membershipSkus.isEmpty()) {
            return membershipSkus;
        }

        membershipSkus = this.membershipPeriods.stream()
                .map(i -> this.generateMembershipSku(membershipId, costInCentsPerMonth, i))
                .collect(Collectors.toList());

        return this.membershipSkuRepository.saveAll(membershipSkus);
    }

    /**
     * Generates membership sku for given info.
     *
     * @param membershipId        Membership id.
     * @param costInCentsPerMonth Costs in cents for a month.
     * @param timeInMonths        Number of months of subscription.
     * @return {@link MembershipSku}.
     */
    private MembershipSku generateMembershipSku(String membershipId,
                                                Long costInCentsPerMonth,
                                                int timeInMonths) {
        return MembershipSku.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .membershipId(membershipId)
                .description(String.format("订阅%d个月", timeInMonths))
                .timeInMonths(timeInMonths)
                .priceInCents(costInCentsPerMonth * timeInMonths)
                .priceInShells(costInCentsPerMonth * timeInMonths)
                .defaultSku(timeInMonths == 3)
                .build();
    }

}