package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.UpdateMembershipSkusByIdRequest;
import com.keepreal.madagascar.vanga.model.MembershipSku;
import com.keepreal.madagascar.vanga.model.ShellSku;
import com.keepreal.madagascar.vanga.model.SponsorSku;
import com.keepreal.madagascar.vanga.model.SupportSku;
import com.keepreal.madagascar.vanga.repository.MembershipSkuRepository;
import com.keepreal.madagascar.vanga.repository.ShellSkuRepository;
import com.keepreal.madagascar.vanga.repository.SponsorSkuRepository;
import com.keepreal.madagascar.vanga.repository.SupportSkuRepository;
import com.keepreal.madagascar.vanga.util.AutoRedisLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.Collections;
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
    private final SupportSkuRepository supportSkuRepository;
    private final SponsorSkuRepository sponsorSkuRepository;
    private final LongIdGenerator idGenerator;
    private final List<Integer> membershipPeriods = Stream.of(1, 3, 6, 12).collect(Collectors.toList());
    private final List<Integer> sponsorPeriods = Stream.of(1, 3, 5).collect(Collectors.toList());
    private final RedissonClient redissonClient;
    private static final String APPLE_SKU_TEMPLATE = "cn.keepreal.feeds.nonrenewal%d";

    /**
     * Constructs the sku service.
     *
     * @param shellSkuRepository      {@link ShellSkuRepository}.
     * @param membershipSkuRepository {@link MembershipSkuRepository}.
     * @param supportSkuRepository    {@link SupportSkuRepository}.
     * @param sponsorSkuRepository    {@link SponsorSkuRepository}
     * @param idGenerator             {@link LongIdGenerator}.
     * @param redissonClient          {@link RedissonClient}.
     */
    public SkuService(ShellSkuRepository shellSkuRepository,
                      MembershipSkuRepository membershipSkuRepository,
                      SupportSkuRepository supportSkuRepository,
                      SponsorSkuRepository sponsorSkuRepository,
                      LongIdGenerator idGenerator,
                      RedissonClient redissonClient) {
        this.shellSkuRepository = shellSkuRepository;
        this.membershipSkuRepository = membershipSkuRepository;
        this.supportSkuRepository = supportSkuRepository;
        this.sponsorSkuRepository = sponsorSkuRepository;
        this.idGenerator = idGenerator;
        this.redissonClient = redissonClient;
    }

    /**
     * Retrieves all active shell skus.
     *
     * @param isWechatPay Whether is wechat pay.
     * @return {@link ShellSku}.
     */
    public List<ShellSku> retrieveShellSkusByActiveIsTrue(Boolean isWechatPay) {
        return this.shellSkuRepository.findAllByActiveIsTrueAndIsWechatPayAndDeletedIsFalseOrderByShellsAsc(isWechatPay);
    }

    /**
     * Retrieves active membership skus for a given membership id.
     *
     * @param membershipId Membership id.
     * @return {@link MembershipSku}.
     */
    public List<MembershipSku> retrieveMembershipSkusByMembershipId(String membershipId) {
        return this.retrieveMembershipSkusByMembershipId(membershipId, false);
    }

    public List<MembershipSku> retrieveMembershipSkusByMembershipId(String membershipId, boolean includeDeleted) {
        return includeDeleted ? this.membershipSkuRepository.findAllByMembershipId(membershipId) :
                this.membershipSkuRepository.findAllByMembershipIdAndDeletedIsFalse(membershipId);
    }

    /**
     * Obsoletes the membership skus.
     *
     * @param membershipId   Membership id.
     * @param membershipSkus {@link MembershipSku}.
     * @param pricePerMonth  New price per month.
     * @return {@link MembershipSku}.
     */
    @Transactional
    public List<MembershipSku> obsoleteMembershipSkusWithNewPrice(String membershipId, List<MembershipSku> membershipSkus, Long pricePerMonth) {
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("obsolete-membershipsku-%s", membershipId))) {
            MembershipSku membershipSku = membershipSkus.get(0);
            List<MembershipSku> newMembershipSkus = this.membershipPeriods.stream()
                    .map(i -> this.generateMembershipSku(membershipSku.getMembershipId(),
                            membershipSku.getMembershipName(),
                            pricePerMonth,
                            membershipSku.getHostId(),
                            membershipSku.getIslandId(),
                            i))
                    .collect(Collectors.toList());
            membershipSkus = membershipSkus.stream().peek(sku -> sku.setDeleted(true)).collect(Collectors.toList());
            this.membershipSkuRepository.saveAll(membershipSkus);
            return this.membershipSkuRepository.saveAll(newMembershipSkus);
        }
    }

    @Transactional
    public List<MembershipSku> obsoleteMembershipSkusWithPermanent(UpdateMembershipSkusByIdRequest request, List<MembershipSku> membershipSkus) {
        if (CollectionUtils.isEmpty(membershipSkus)) {
            return Collections.emptyList();
        }

        MembershipSku sku = membershipSkus.get(0);
        String membershipId = request.getMembershipId();

        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("obsolete-membershipsku-%s", membershipId))) {
            List<MembershipSku> newMembershipSkus = Collections.singletonList(this.generatePermanentMembershipSku(
                    membershipId,
                    request.hasMembershipName() ? request.getMembershipName().getValue() : sku.getMembershipName(),
                    request.hasPricePerMonth() ? request.getPricePerMonth().getValue() : sku.getPriceInCents(),
                    sku.getHostId(),
                    sku.getIslandId(),
                    request.hasActive() ? request.getActive().getValue() : sku.getActive()
            ));

            membershipSkus = membershipSkus.stream().peek(membershipSku -> membershipSku.setDeleted(true)).collect(Collectors.toList());
            this.membershipSkuRepository.saveAll(membershipSkus);
            return this.membershipSkuRepository.saveAll(newMembershipSkus);
        }
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
     * Deletes membership skus.
     *
     * @param membershipId Membership id.
     */
    public void deleteMembershipSkusByMembershipId(String membershipId) {
        List<MembershipSku> membershipSkus = this.membershipSkuRepository.findAllByMembershipIdAndDeletedIsFalse(membershipId);
        membershipSkus = membershipSkus.stream().peek(membershipSku -> membershipSku.setDeleted(true)).collect(Collectors.toList());
        this.membershipSkuRepository.saveAll(membershipSkus);
    }

    /**
     * Updates all membership skus.
     *
     * @param membershipSkus {@link MembershipSku}.
     */
    @Transactional
    public List<MembershipSku> updateAll(Iterable<MembershipSku> membershipSkus) {
        return this.membershipSkuRepository.saveAll(membershipSkus);
    }

    /**
     * Updates all sponsor skus.
     *
     * @param sponsorSkus {@link SponsorSku}.
     */
    @Transactional
    public List<SponsorSku> updateAllSponsorSkus(Iterable<SponsorSku> sponsorSkus) {
        return this.sponsorSkuRepository.saveAll(sponsorSkus);
    }

    /**
     * Creates default membership skus if not exists.
     *
     * @param membershipId        Membership id.
     * @param membershipName      Membership name.
     * @param hostId              Host id.
     * @param islandId            Island id.
     * @param costInCentsPerMonth Membership cost in cents per month.
     * @return {@link MembershipSku}.
     */
    @Transactional
    public List<MembershipSku> createDefaultMembershipSkusByMembershipIdAndCostPerMonth(String membershipId,
                                                                                        String membershipName,
                                                                                        String hostId,
                                                                                        String islandId,
                                                                                        Long costInCentsPerMonth,
                                                                                        boolean permanent) {
        List<MembershipSku> membershipSkus = this.retrieveMembershipSkusByMembershipIdAndActiveIsTrue(membershipId);
        if (Objects.nonNull(membershipSkus) && !membershipSkus.isEmpty()) {
            return membershipSkus;
        }

        if (permanent) {
            membershipSkus = Collections.singletonList(this.generatePermanentMembershipSku(membershipId, membershipName, costInCentsPerMonth, hostId, islandId, true));
        } else {
            membershipSkus = this.membershipPeriods.stream()
                    .map(i -> this.generateMembershipSku(membershipId, membershipName, costInCentsPerMonth, hostId, islandId, i))
                    .collect(Collectors.toList());
        }

        return this.membershipSkuRepository.saveAll(membershipSkus);
    }

    /**
     * Retrieves the membership sku by id.
     *
     * @param membershipSkuId Membership sku id.
     * @return {@link MembershipSku}.
     * @implNote Do not filter by is deleted since the payment may happen during the obsoleting of a sku,
     * or a proper error prorogation should be implemented.
     */
    public MembershipSku retrieveMembershipSkuById(String membershipSkuId) {
        return this.membershipSkuRepository.findById(membershipSkuId).orElse(null);
    }

    /**
     * Retrieves the membership skus by ids including deleted ones.
     *
     * @param ids Ids.
     * @return {@link MembershipSku}.
     */
    public List<MembershipSku> retrieveMembershipSkusByIds(Iterable<String> ids) {
        return this.membershipSkuRepository.findAllById(ids);
    }

    /**
     * Retrieves the shell sku by id.
     *
     * @param shellSkuId Shell sku id,
     * @return {@link ShellSku}.
     * @implNote Do not filter by is deleted since the payment may happen during the obsoleting of a sku,
     * or a proper error prorogation should be implemented.
     */
    public ShellSku retrieveShellSkuById(String shellSkuId) {
        return this.shellSkuRepository.findById(shellSkuId).orElse(null);
    }

    public List<SupportSku> retrieveSupportSkus() {
        return this.supportSkuRepository.findAllByActiveIsTrueAndDeletedIsFalseOrderByPriceInCents();
    }

    /**
     * Creates default sponsor skus if not exists.
     *
     * @param sponsorId             sponsor id.
     * @param hostId                host id.
     * @param islandId              island id.
     * @param priceInCentsPeUnit    unit price.
     * @param giftId                gift image id.
     * @return {@link SponsorSku}.
     */
    @Transactional
    public List<SponsorSku> createDefaultSponsorSkusBySponsorIdAndPricePerUnit(String sponsorId,
                                                                               String hostId,
                                                                               String islandId,
                                                                               Long priceInCentsPeUnit,
                                                                               String giftId) {
        List<SponsorSku> sponsorSkus = this.retrieveSponsorSkusBySponsorIdAndActiveIsTrue(sponsorId);
        if (Objects.nonNull(sponsorSkus) && !sponsorSkus.isEmpty()) {
            return sponsorSkus;
        }

        sponsorSkus = this.sponsorPeriods.stream()
                    .map(i -> this.generateSponsorSku(sponsorId, hostId, islandId, giftId, priceInCentsPeUnit, i, false))
                    .collect(Collectors.toList());
        //自定义金额 sku 总价即为单价 方便计算礼物个数(取整)
        sponsorSkus.add(this.generateSponsorSku(sponsorId, hostId, islandId, giftId, priceInCentsPeUnit, 1, true));

        return this.sponsorSkuRepository.saveAll(sponsorSkus);
    }

    /**
     * Retrieves active sponsor skus for a given sponsor id.
     *
     * @param sponsorId sponsor id.
     * @return {@link SponsorSku}.
     */
    public List<SponsorSku> retrieveSponsorSkusBySponsorIdAndActiveIsTrue(String sponsorId) {
        return this.sponsorSkuRepository.findAllBySponsorIdAndActiveIsTrueAndDeletedIsFalse(sponsorId);
    }

    /**
     * Retrieves sponsor skus for a given sponsor id.
     *
     * @param sponsorId sponsor id.
     * @return {@link SponsorSku}.
     */
    public List<SponsorSku> retrieveSponsorSkusBySponsorId(String sponsorId) {
        return this.sponsorSkuRepository.findAllBySponsorIdAndDeletedIsFalse(sponsorId);
    }

    /**
     * Obsoletes the sponsor skus.
     *
     * @param sponsorId   Sponsor id.
     * @param sponsorSkus {@link SponsorSku}.
     * @param pricePerUnit  New price per unit.
     * @return {@link SponsorSku}.
     */
    @Transactional
    public List<SponsorSku> obsoleteSponsorSkusWithNewUnitPrice(String sponsorId, List<SponsorSku> sponsorSkus, Long pricePerUnit) {
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, String.format("obsolete-sponsorsku-%s", sponsorId))) {
            SponsorSku sponsorSku = sponsorSkus.get(0);
            List<SponsorSku> newSponsorSkus = this.sponsorPeriods.stream()
                    .map(i -> this.generateSponsorSku(sponsorSku.getSponsorId(),
                            sponsorSku.getHostId(),
                            sponsorSku.getIslandId(),
                            sponsorSku.getGiftId(),
                            pricePerUnit,
                            i,
                            false))
                    .collect(Collectors.toList());
            newSponsorSkus.add(this.generateSponsorSku(sponsorId,
                    sponsorSku.getHostId(),
                    sponsorSku.getIslandId(),
                    sponsorSku.getGiftId(),
                    pricePerUnit,
                    1,
                    true));
            sponsorSkus = sponsorSkus.stream().peek(sku -> sku.setDeleted(true)).collect(Collectors.toList());
            this.sponsorSkuRepository.saveAll(sponsorSkus);
            return this.sponsorSkuRepository.saveAll(newSponsorSkus);
        }
    }

    /**
     * Retrieves sponsor skus for a given island id.
     *
     * @param islandId island id.
     * @return {@link SponsorSku}.
     */
    public List<SponsorSku> retrieveSponsorSkusByIslandId(String islandId) {
        return this.sponsorSkuRepository.findAllByIslandIdAndActiveIsTrueAndDeletedIsFalse(islandId);
    }

    /**
     * Retrieves sponsor sku by id.
     *
     * @param id sku id
     * @return {@link SponsorSku}
     */
    public SponsorSku retrieveSponsorSkuById(String id) {
        return this.sponsorSkuRepository.findTopByIdAndDeletedIsFalse(id);
    }

    /**
     * Generates membership sku for given info.
     *
     * @param membershipId        Membership id.
     * @param membershipName      Membership name.
     * @param costInCentsPerMonth Costs in cents for a month.
     * @param hostId              Host id.
     * @param islandId            Island id.
     * @param timeInMonths        Number of months of subscription.
     * @return {@link MembershipSku}.
     */
    private MembershipSku generateMembershipSku(String membershipId,
                                                String membershipName,
                                                Long costInCentsPerMonth,
                                                String hostId,
                                                String islandId,
                                                int timeInMonths) {
        return MembershipSku.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .membershipId(membershipId)
                .membershipName(membershipName)
                .description(String.format("订阅%d个月", timeInMonths))
                .timeInMonths(timeInMonths)
                .priceInCents(costInCentsPerMonth * timeInMonths)
                .priceInShells(costInCentsPerMonth * timeInMonths)
                .defaultSku(timeInMonths == 3)
                .hostId(hostId)
                .islandId(islandId)
                .appleSkuId(String.format(SkuService.APPLE_SKU_TEMPLATE, costInCentsPerMonth * timeInMonths))
                .build();
    }

    private MembershipSku generatePermanentMembershipSku(String membershipId,
                                                         String membershipName,
                                                         Long price,
                                                         String hostId,
                                                         String islandId,
                                                         Boolean active) {
        return MembershipSku.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .membershipId(membershipId)
                .membershipName(membershipName)
                .description("永久有效")
                .timeInMonths(1)
                .priceInCents(price)
                .priceInShells(price)
                .defaultSku(true)
                .hostId(hostId)
                .islandId(islandId)
                .active(active)
                .appleSkuId(String.format(SkuService.APPLE_SKU_TEMPLATE, price))
                .permanent(true)
                .build();
    }

    /**
     * Generates sponsor sku for given info.
     *
     * @param sponsorId   sponsor id.
     * @param hostId      host id.
     * @param islandId    island id.
     * @param giftId      gift id.
     * @param priceInCentsPerUnit   unit price.
     * @param quantity    number of gift.
     * @param isCustomSku whether custom amount.
     * @return {@link SponsorSku}.
     */
    private SponsorSku generateSponsorSku(String sponsorId,
                                          String hostId,
                                          String islandId,
                                          String giftId,
                                          long priceInCentsPerUnit,
                                          long quantity,
                                          boolean isCustomSku) {
        return SponsorSku.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .sponsorId(sponsorId)
                .islandId(islandId)
                .hostId(hostId)
                .giftId(giftId)
                .priceInCents(priceInCentsPerUnit * quantity)
                .quantity(quantity)
                .customSku(isCustomSku)
                .defaultSku(quantity == Constants.DEFAULT_SPONSOR_SKU_QUANTITY && !isCustomSku)
                .build();
    }

}
