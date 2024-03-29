package com.keepreal.madagascar.coua.service;


import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.coua.UpdateMembershipRequest;
import com.keepreal.madagascar.coua.dao.MembershipInfoRepository;
import com.keepreal.madagascar.coua.model.MembershipInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents the membership service.
 */
@Service
public class MembershipService {

    private final MembershipInfoRepository repository;
    private final LongIdGenerator idGenerator;
    private final SubscribeMembershipService subscribeMembershipService;
    private final SubscriptionService subscriptionService;
    private final SkuService skuService;
    private final ChatService chatService;

    /**
     * Constructor the membership service.
     *
     * @param repository                 {@link MembershipInfoRepository}.
     * @param idGenerator                {@link LongIdGenerator}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     * @param subscriptionService        {@link SubscriptionService}.
     * @param skuService                 {@link SkuService}.
     * @param chatService                {@link ChatService}.
     */
    public MembershipService(MembershipInfoRepository repository,
                             LongIdGenerator idGenerator,
                             SubscribeMembershipService subscribeMembershipService,
                             SubscriptionService subscriptionService,
                             SkuService skuService, ChatService chatService) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.subscribeMembershipService = subscribeMembershipService;
        this.subscriptionService = subscriptionService;
        this.skuService = skuService;
        this.chatService = chatService;
    }

    public MembershipInfo createMembership(MembershipInfo membershipInfo) {
        membershipInfo.setId(String.valueOf(idGenerator.nextId()));
        List<Integer> colorTypeList = repository.getColorTypeListByIslandId(membershipInfo.getIslandId());
        ArrayList<Integer> defaultColorList = new ArrayList<>(Arrays.asList(1, 2, 3, 1, 2, 3, 1, 2, 3));
        colorTypeList.forEach(defaultColorList::remove);
        membershipInfo.setColorType(defaultColorList.get(0));

        this.skuService.createMembershipSkusByMembershipId(membershipInfo.getId(),
                membershipInfo.getName(),
                membershipInfo.getPricePerMonth(),
                membershipInfo.getHostId(),
                membershipInfo.getIslandId(),
                membershipInfo.getPermanent());

        return repository.save(membershipInfo);
    }

    public MembershipInfo getMembershipById(String id) {
        return this.getMembershipById(id, false);
    }

    public MembershipInfo getMembershipById(String id, boolean includeInactive) {
        return includeInactive ? repository.findMembershipInfoByIdAndDeletedIsFalse(id) :
                repository.findMembershipInfoByIdAndActiveIsTrueAndDeletedIsFalse(id);
    }

    public List<MembershipInfo> getMembershipListByIslandId(String islandId, boolean includeInactive) {
        if (includeInactive) {
            return repository.findMembershipInfosByIslandIdAndDeletedIsFalseOrderByTopDescActiveDescPricePerMonthAsc(islandId);
        } else {
            return repository.findMembershipInfosByIslandIdAndActiveIsTrueAndDeletedIsFalseOrderByTopDescActiveDescPricePerMonthAsc(islandId);
        }
    }

    public List<MembershipInfo> getMembershipListByHostId(String userId) {
        return repository.findMembershipInfosByHostIdAndDeletedIsFalse(userId);
    }

    public List<MembershipInfo> getMembershipListByIslandIds(List<String> islandIds) {
        return repository.findMembershipInfosByIslandIdInAndActiveIsTrueAndDeletedIsFalseOrderByTopDescActiveDescPricePerMonthAsc(islandIds);
    }

    public List<MembershipInfo> getMembershipListByIds(Iterable<String> ids) {
        return this.repository.findAllByIdInAndDeletedIsFalse(ids);
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
        if (!StringUtils.isEmpty(islandId) && repository.countByIslandIdAndDeletedIsFalse(islandId) > 0) {
            list.add(FeedMembershipMessage.newBuilder()
                    .setName("所有支持者")
                    .setMemberCount(subscribeMembershipService.getMemberCountByIslandId(islandId))
                    .build());
        }
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
                .setPermanent(membershipInfo.getPermanent())
                .setActive(membershipInfo.getActive())
                .build();
    }

    public MembershipMessage getMembershipMessage(MembershipInfo membershipInfo) {
        if (Objects.isNull(membershipInfo)) {
            return null;
        }

        return MembershipMessage.newBuilder()
                .setId(membershipInfo.getId())
                .setHostId(membershipInfo.getHostId())
                .setIslandId(membershipInfo.getIslandId())
                .setDescription(membershipInfo.getDescription())
                .setPricePerMonth(membershipInfo.getPricePerMonth())
                .setName(membershipInfo.getName())
                .setColorType(membershipInfo.getColorType())
                .setIsTop(membershipInfo.getTop())
                .setMemberCount(membershipInfo.getMemberCount())
                .setUseCustomMessage(membershipInfo.getUseCustomMessage())
                .setMessage(membershipInfo.getMessage())
                .setPermanent(membershipInfo.getPermanent())
                .setImageUri(membershipInfo.getImageUri())
                .setWidth(membershipInfo.getWidth())
                .setHeight(membershipInfo.getHeight())
                .setSize(membershipInfo.getSize())
                .setActivate(membershipInfo.getActive())
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
        this.chatService.deleteChatgroupMembershipByMembershipId(membership.getId());
    }

    /**
     * Updates a membership as deactivated.
     *
     * @param membership {@link MembershipInfo}.
     */
    public void deactivateMembership(MembershipInfo membership, boolean deactivate) {
        membership.setTop(false);
        membership.setActive(!deactivate);
        this.updateMembership(membership);

        this.skuService.updateMembershipSkusByMembershipId(membership.getId(), membership.getName(), membership.getPricePerMonth(), !deactivate, membership.getPermanent());
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

        if (request.hasPermanent()) {
            membershipInfo.setPermanent(request.getPermanent().getValue());
        }

        if (request.hasDescription()) {
            membershipInfo.setDescription(request.getDescription().getValue());
        }

        if (request.hasUseCustomMessage()) {
            membershipInfo.setUseCustomMessage(request.getUseCustomMessage().getValue());
            if (request.hasMessage()) {
                membershipInfo.setMessage(request.getMessage().getValue());
            }
        }

        if (request.hasImageUri()) {
            membershipInfo.setImageUri(request.getImageUri().getValue());
        }

        if (request.hasWidth()) {
            membershipInfo.setWidth(request.getWidth().getValue());
        }
        if (request.hasHeight()) {
            membershipInfo.setHeight(request.getHeight().getValue());
        }
        if (request.hasSize()) {
            membershipInfo.setSize(request.getSize().getValue());
        }

        this.skuService.updateMembershipSkusByMembershipId(request.getId(), newName, newPrice, null, membershipInfo.getPermanent());
        return this.updateMembership(membershipInfo);
    }

    public void addMemberCount(String membershipId) {
        this.repository.addMemberCountByMembershipId(membershipId);
    }
}
