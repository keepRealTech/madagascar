package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.coua.SponsorMessage;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.vanga.SupportMessage;
import org.springframework.stereotype.Component;
import swagger.model.SponsorDTO;
import swagger.model.SupportDTO;
import swagger.model.SupportDTOV2;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupportDTOFactory {

    private final MembershipDTOFactory membershipDTOFactory;
    private final MembershipService membershipService;
    private final SponsorDTOFactory sponsorDTOFactory;

    public SupportDTOFactory(MembershipDTOFactory membershipDTOFactory,
                             MembershipService membershipService,
                             SponsorDTOFactory sponsorDTOFactory) {
        this.membershipDTOFactory = membershipDTOFactory;
        this.membershipService = membershipService;
        this.sponsorDTOFactory = sponsorDTOFactory;
    }

    public SupportDTO valueOf(SupportMessage message, String islandId) {
        SupportDTO dto = new SupportDTO();
        List<MembershipMessage> membershipMessages = this.membershipService.retrieveMembershipsByIslandId(islandId, false);
        dto.setMemberships(membershipMessages.stream().map(this.membershipDTOFactory::valueOf).collect(Collectors.toList()));
        dto.setSponsors(this.sponsorValueOf(message));
        return dto;
    }

    public SponsorDTO sponsorValueOf(SupportMessage message) {
        SponsorDTO dto = new SponsorDTO();
        dto.setCount(message.getCount());
        dto.setText(message.getText());
        return dto;
    }

    public SupportDTOV2 valueOf (SponsorMessage sponsorMessage, String islandId) {
        SupportDTOV2 supportDTOV2 = new SupportDTOV2();
        List<MembershipMessage> membershipMessages = this.membershipService.retrieveMembershipsByIslandId(islandId, false);
        supportDTOV2.setMemberships(membershipMessages.stream().map(this.membershipDTOFactory::valueOf).collect(Collectors.toList()));
        supportDTOV2.setSponsors(this.sponsorDTOFactory.valueOf(sponsorMessage));
        return supportDTOV2;
    }
}
