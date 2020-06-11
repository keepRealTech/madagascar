package com.keepreal.madagascar.coua.service;


import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.coua.UpdateMembershipRequest;
import com.keepreal.madagascar.coua.dao.MembershipInfoRepository;
import com.keepreal.madagascar.coua.model.MembershipInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the membership service.
 */
@Service
public class MembershipService {

    private final MembershipInfoRepository repository;
    private final LongIdGenerator idGenerator;
    private final SubscribeMembershipService subscribeMembershipService;
    private final SubscriptionService subscriptionService;
    private final List<Integer> defaultColorTypeList;
    private final SkuService skuService;

    /**
     * Constructor the membership service.
     *
     * @param repository                 {@link MembershipInfoRepository}.
     * @param idGenerator                {@link LongIdGenerator}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     * @param subscriptionService        {@link SubscriptionService}.
     * @param skuService                 {@link SkuService}.
     */
    public MembershipService(MembershipInfoRepository repository,
                             LongIdGenerator idGenerator,
                             SubscribeMembershipService subscribeMembershipService,
                             SubscriptionService subscriptionService,
                             SkuService skuService) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.subscribeMembershipService = subscribeMembershipService;
        this.subscriptionService = subscriptionService;
        this.skuService = skuService;
        this.defaultColorTypeList = new ArrayList<>(Arrays.asList(1, 2, 3, 1, 2));
    }

    public MembershipInfo createMembership(MembershipInfo membershipInfo) {
        membershipInfo.setId(String.valueOf(idGenerator.nextId()));
        List<Integer> colorTypeList = repository.getColorTypeListByIslandId(membershipInfo.getIslandId());
        colorTypeList.forEach(defaultColorTypeList::remove);
        membershipInfo.setColorType(defaultColorTypeList.get(0));

        this.skuService.createMembershipSkusByMembershipId(membershipInfo.getId(), membershipInfo.getName(),
                membershipInfo.getPricePerMonth(), membershipInfo.getHostId(), membershipInfo.getIslandId());

        return repository.save(membershipInfo);
    }

    public MembershipInfo getMembershipById(String id) {
        return repository.findMembershipInfoByIdAndActiveIsTrueAndDeletedIsFalse(id);
    }

    public List<MembershipInfo> getMembershipListByIslandId(String islandId) {
        return repository.findMembershipInfosByIslandIdAndActiveIsTrueAndDeletedIsFalseOrderByTopDescPricePerMonthAsc(islandId);
    }

    public MembershipInfo updateMembership(MembershipInfo membershipInfo) {
        return repository.save(membershipInfo);
    }

    public List<FeedMembershipMessage> generateBaseMessage(String islandId) {
        List<FeedMembershipMessage> list = new ArrayList<>();
        list.add(FeedMembershipMessage.newBuilder()
                .setName("所有岛民")
                .setMemberCount(subscriptionService.getMemberCountByIslandId(islandId))
                .build());
        list.add(FeedMembershipMessage.newBuilder()
                .setName("所有会员")
                .setMemberCount(subscribeMembershipService.getMemberCountByIslandId(islandId))
                .build());
        return list;
    }

    public void revokeTopMembership(String islandId) {
        MembershipInfo membershipInfo = repository.findMembershipInfoByIslandIdAndTopIsTrueAndActiveIsTrueAndDeletedIsFalse(islandId);
        if (membershipInfo != null) {
            membershipInfo.setTop(false);
            repository.save(membershipInfo);
        }
    }

    public FeedMembershipMessage getFeedMembershipMessage(MembershipInfo membershipInfo) {
        return FeedMembershipMessage.newBuilder()
                .setId(membershipInfo.getId())
                .setName(membershipInfo.getName())
                .setPricePerMonth(membershipInfo.getPricePerMonth())
                .setMemberCount(subscribeMembershipService.getMemberCountByMembershipId(membershipInfo.getId()))
                .build();
    }

    public MembershipMessage getMembershipMessage(MembershipInfo membershipInfo) {
        return MembershipMessage.newBuilder()
                .setId(membershipInfo.getId())
                .setHostId(membershipInfo.getHostId())
                .setIslandId(membershipInfo.getIslandId())
                .setDescription(membershipInfo.getDescription())
                .setPricePerMonth(membershipInfo.getPricePerMonth())
                .setName(membershipInfo.getName())
                .setColorType(membershipInfo.getColorType())
                .setIsTop(membershipInfo.getTop())
                .setMemberCount(subscribeMembershipService.getMemberCountByMembershipId(membershipInfo.getId()))
                .build();
    }

    /**
     * Marks a membership as deleted.
     *
     * @param membership {@link MembershipInfo}.
     */
    public void deleteMembership(MembershipInfo membership) {
        membership.setDeleted(true);
        this.skuService.deleteMembershipSkusByMembershipId(membership.getId());
        this.updateMembership(membership);
    }

    /**
     * Updates a membership as deactivated.
     *
     * @param membership {@link MembershipInfo}.
     */
    public void deactivateMembership(MembershipInfo membership) {
        membership.setActive(false);
        this.skuService.updateMembershipSkusByMembershipId(membership.getId(), null, null, true);
        this.updateMembership(membership);
    }

    /**
     * Updates a membership as well as its skus.
     *
     * @param membershipInfo {@link MembershipInfo}.
     * @param request        {@link UpdateMembershipRequest}.
     * @return {@link MembershipInfo}.
     */
    public MembershipInfo updateMembershipWithSku(MembershipInfo membershipInfo, UpdateMembershipRequest request) {
        String newName = null;
        if (request.hasName()) {
            newName = request.getName().getValue();
            membershipInfo.setName(newName);
        }

        Integer newPrice = null;
        if (request.hasPricePerMonth()) {
            newPrice = request.getPricePerMonth().getValue();
            membershipInfo.setPricePerMonth(newPrice);
        }

        if (request.hasDescription()) {
            membershipInfo.setDescription(request.getDescription().getValue());
        }

        this.skuService.updateMembershipSkusByMembershipId(request.getId(), newName, newPrice, null);
        return this.updateMembership(membershipInfo);
    }

}
