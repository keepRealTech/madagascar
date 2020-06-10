package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import swagger.model.BriefMembershipDTO;
import swagger.model.FeedMembershipDTO;
import swagger.model.MembershipDTO;

import java.util.Arrays;

/**
 * Represents the membership dto factory.
 */
@Component
@Slf4j
public class MembershipDTOFactory {

    public BriefMembershipDTO briefValueOf(MembershipMessage membershipMessage) {
        BriefMembershipDTO dto = new BriefMembershipDTO();
        dto.setId(membershipMessage.getId());
        dto.setIslandId(membershipMessage.getIslandId());
        dto.setHostId(membershipMessage.getHostId());
        dto.setDescription(Arrays.asList(membershipMessage.getDescription().split(",")));
        dto.setMenbershipName(membershipMessage.getName());
        dto.setColorType(membershipMessage.getColorType());
        dto.setChargePerMonth(membershipMessage.getPricePreMonth());
        return dto;
    }

    public FeedMembershipDTO feedValueOf(FeedMembershipMessage feedMembershipMessage) {
        FeedMembershipDTO dto = new FeedMembershipDTO();
        dto.setId(feedMembershipMessage.getId());
        dto.setMemberCount(feedMembershipMessage.getMemberCount());
        dto.setChargePerMonth(feedMembershipMessage.getPricePreMonth());
        dto.setMembershipName(feedMembershipMessage.getName());
        return dto;
    }

    public MembershipDTO valueOf(MembershipMessage membershipMessage) {
        MembershipDTO dto = new MembershipDTO();
        dto.setId(membershipMessage.getId());
        dto.setHostId(membershipMessage.getHostId());
        dto.setIslandId(membershipMessage.getIslandId());
        dto.setChargePerMonth(membershipMessage.getPricePreMonth());
        dto.setDescription(Arrays.asList(membershipMessage.getDescription().split(",")));
        dto.setMenbershipName(membershipMessage.getName());
        dto.setColorType(membershipMessage.getColorType());
        dto.setIsTop(membershipMessage.getIsTop());
        dto.setMemberCount(membershipMessage.getMemberCount());
        return dto;
    }
}