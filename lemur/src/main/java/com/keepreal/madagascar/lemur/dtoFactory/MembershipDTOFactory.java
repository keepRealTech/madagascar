package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import swagger.model.BriefMembershipDTO;
import swagger.model.FeedMembershipDTO;
import swagger.model.MembershipDTO;
import swagger.model.MembershipTemplateDTO;
import swagger.model.SimpleMembershipDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the membership dto factory.
 */
@Component
@Slf4j
public class MembershipDTOFactory {

    private final List<String> membershipTemplateList = Arrays.asList("最新作品抢先看",
            "解锁我的日常动态",
            "解锁独家番外、花絮和福利",
            "解锁专属群聊",
            "解锁提问箱",
            "为你私人订制角色、歌曲、视频",
            "在我的下期内容里感谢你",
            "获得实物折扣或特权（门票、明信片、优先购买权");

    public SimpleMembershipDTO simpleValueOf(MembershipMessage membershipMessage) {
        if (Objects.isNull(membershipMessage)) {
            return null;
        }

        SimpleMembershipDTO dto = new SimpleMembershipDTO();
        dto.id(membershipMessage.getId());
        dto.setMembershipName(membershipMessage.getName());
        dto.setColorType(membershipMessage.getColorType());
        dto.setPricePerMonthInCents((long) membershipMessage.getPricePerMonth());
        return dto;
    }

    public BriefMembershipDTO briefValueOf(MembershipMessage membershipMessage) {
        if (Objects.isNull(membershipMessage)) {
            return null;
        }

        BriefMembershipDTO dto = new BriefMembershipDTO();
        dto.setId(membershipMessage.getId());
        dto.setIslandId(membershipMessage.getIslandId());
        dto.setHostId(membershipMessage.getHostId());
        dto.setDescription(Arrays.asList(membershipMessage.getDescription().split(",")));
        dto.setMembershipName(membershipMessage.getName());
        dto.setColorType(membershipMessage.getColorType());
        dto.setChargePerMonth(membershipMessage.getPricePerMonth());
        dto.setUseCustomMessage(membershipMessage.getUseCustomMessage());
        dto.setMessage(membershipMessage.getMessage());
        return dto;
    }

    public FeedMembershipDTO feedValueOf(FeedMembershipMessage feedMembershipMessage) {
        if (Objects.isNull(feedMembershipMessage)) {
            return null;
        }

        FeedMembershipDTO dto = new FeedMembershipDTO();
        dto.setId(feedMembershipMessage.getId());
        dto.setMemberCount(feedMembershipMessage.getMemberCount());
        dto.setChargePerMonth(feedMembershipMessage.getPricePerMonth());
        dto.setMembershipName(feedMembershipMessage.getName());
        return dto;
    }

    public MembershipDTO valueOf(MembershipMessage membershipMessage) {
        if (Objects.isNull(membershipMessage)) {
            return null;
        }

        MembershipDTO dto = new MembershipDTO();
        dto.setId(membershipMessage.getId());
        dto.setHostId(membershipMessage.getHostId());
        dto.setIslandId(membershipMessage.getIslandId());
        dto.setChargePerMonth(membershipMessage.getPricePerMonth());
        dto.setDescription(Arrays.asList(membershipMessage.getDescription().split(",")));
        dto.setMembershipName(membershipMessage.getName());
        dto.setColorType(membershipMessage.getColorType());
        dto.setIsTop(membershipMessage.getIsTop());
        dto.setMemberCount(membershipMessage.getMemberCount());
        dto.setUseCustomMessage(membershipMessage.getUseCustomMessage());
        dto.setMessage(membershipMessage.getMessage());
        return dto;
    }

    public List<MembershipTemplateDTO> listValueOf() {
        return membershipTemplateList.stream().map(template -> {
            MembershipTemplateDTO dto = new MembershipTemplateDTO();
            dto.setText(template);
            return dto;
        }).collect(Collectors.toList());
    }
}
