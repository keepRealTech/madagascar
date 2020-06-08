package com.keepreal.madagascar.coua.service;


import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
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

    /**
     * Constructor the membership service.
     *
     * @param repository                    {@link MembershipInfoRepository}.
     * @param idGenerator                   {@link LongIdGenerator}.
     * @param subscribeMembershipService    {@link SubscribeMembershipService}.
     * @param subscriptionService           {@link SubscriptionService}.
     */
    public MembershipService(MembershipInfoRepository repository,
                             LongIdGenerator idGenerator,
                             SubscribeMembershipService subscribeMembershipService,
                             SubscriptionService subscriptionService) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.subscribeMembershipService = subscribeMembershipService;
        this.subscriptionService = subscriptionService;
        this.defaultColorTypeList = new ArrayList<>(Arrays.asList(1, 2, 3, 1, 2));
    }

    public MembershipInfo createMembership(MembershipInfo membershipInfo) {
        membershipInfo.setId(String.valueOf(idGenerator.nextId()));
        List<Integer> colorTypeList = repository.getColorTypeListByIslandId(membershipInfo.getIslandId());
        colorTypeList.forEach(defaultColorTypeList::remove);
        membershipInfo.setColorType(defaultColorTypeList.get(0));
        return repository.save(membershipInfo);
    }

    public MembershipInfo getMembershipById(String id) {
        return repository.findMembershipInfoByIdAndActivateIsTrueAndDeletedIsFalse(id);
    }

    public List<MembershipInfo> getMembershipListByIslandId(String islandId) {
        return repository.findMembershipInfosByIslandIdAndActivateIsTrueAndDeletedIsFalse(islandId);
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
        MembershipInfo membershipInfo = repository.findMembershipInfoByIslandIdAndTopIsTrue(islandId);
        membershipInfo.setTop(false);
        repository.save(membershipInfo);
    }

    public FeedMembershipMessage getFeedMembershipMessage(MembershipInfo membershipInfo) {
        return FeedMembershipMessage.newBuilder()
                .setId(membershipInfo.getId())
                .setName(membershipInfo.getName())
                .setPricePreMonth(membershipInfo.getPricePreMonth())
                .setMemberCount(subscribeMembershipService.getMemberCountByMembershipId(membershipInfo.getId()))
                .build();
    }

    public MembershipMessage getMembershipMessage(MembershipInfo membershipInfo) {
        return MembershipMessage.newBuilder()
                .setId(membershipInfo.getId())
                .setHostId(membershipInfo.getHostId())
                .setIslandId(membershipInfo.getIslandId())
                .setDescription(membershipInfo.getDescription())
                .setPricePreMonth(membershipInfo.getPricePreMonth())
                .setName(membershipInfo.getName())
                .setColorType(membershipInfo.getColorType())
                .setIsTop(membershipInfo.getTop())
                .setMemberCount(subscribeMembershipService.getMemberCountByMembershipId(membershipInfo.getId()))
                .build();
    }
}
