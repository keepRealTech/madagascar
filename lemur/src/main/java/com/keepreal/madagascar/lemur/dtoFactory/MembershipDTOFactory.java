package com.keepreal.madagascar.lemur.dtoFactory;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.asity.IslandChatgroupsResponse;
import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.lemur.service.BoxService;
import com.keepreal.madagascar.lemur.service.ChatService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.SubscribeMembershipService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.vanga.SubscribeMembershipMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import swagger.model.BriefMembershipDTO;
import swagger.model.FeedMembershipDTO;
import swagger.model.MembershipDTO;
import swagger.model.MembershipIconType;
import swagger.model.MembershipTemplateDTO;
import swagger.model.MyMembershipDTO;
import swagger.model.SimpleMembershipDTO;
import swagger.model.WithExpiredMembershipDTO;

import java.util.ArrayList;
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

    private final SubscribeMembershipService subscribeMembershipService;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;
    private final IslandService islandService;
    private final MembershipService membershipService;
    private final BoxService boxService;
    private final ChatService chatService;
    private final MultiMediaDTOFactory multiMediaDTOFactory;

    /**
     * Constructs the membershipDTOFactory.
     *
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     * @param userService                {@link UserService}.
     * @param userDTOFactory             {@link UserDTOFactory}.
     * @param islandService              {@link IslandService}.
     * @param membershipService          {@link MembershipService}.
     * @param boxService                 {@link BoxService}.
     * @param chatService                {@link ChatService}.
     * @param multiMediaDTOFactory       {@link MultiMediaDTOFactory}.
     */
    public MembershipDTOFactory(SubscribeMembershipService subscribeMembershipService,
                                UserService userService,
                                UserDTOFactory userDTOFactory,
                                IslandService islandService,
                                MembershipService membershipService,
                                BoxService boxService,
                                ChatService chatService,
                                MultiMediaDTOFactory multiMediaDTOFactory) {
        this.subscribeMembershipService = subscribeMembershipService;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
        this.islandService = islandService;
        this.membershipService = membershipService;
        this.boxService = boxService;
        this.chatService = chatService;
        this.multiMediaDTOFactory = multiMediaDTOFactory;
    }

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
        dto.setIsPermanent(membershipMessage.getPermanent());
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
        dto.setIsPermanent(feedMembershipMessage.getPermanent());
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
        dto.setIsPermanent(membershipMessage.getPermanent());
        dto.setImage(this.multiMediaDTOFactory.valueOf(membershipMessage));
        return dto;
    }

    public List<MembershipTemplateDTO> listValueOf() {
        return membershipTemplateList.stream().map(template -> {
            MembershipTemplateDTO dto = new MembershipTemplateDTO();
            dto.setText(template);
            return dto;
        }).collect(Collectors.toList());
    }

    public MyMembershipDTO valueOf(String userId, String islandId) {
        MyMembershipDTO dto = new MyMembershipDTO();
        List<SubscribeMembershipMessage> messageList = this.subscribeMembershipService.retrieveSubscribeMembership(userId, islandId);
        if (messageList.isEmpty()) {
            return dto;
        }

        dto.setUser(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(userId)));
        dto.setHost(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(this.islandService.retrieveIslandById(islandId).getHostId())));
        dto.setMemberships(messageList.stream().map(this::valueOf).collect(Collectors.toList()));
        dto.setIcons(list(islandId, messageList));

        return dto;
    }

    private WithExpiredMembershipDTO valueOf(SubscribeMembershipMessage message) {
        WithExpiredMembershipDTO dto = new WithExpiredMembershipDTO();
        MembershipMessage membershipMessage = this.membershipService.retrieveMembershipById(message.getMembershipId());
        dto.setId(membershipMessage.getId());
        dto.setMembershipName(membershipMessage.getName());
        dto.setIsPermanent(membershipMessage.getPermanent());
        dto.setExpireTime(message.getExpiredTime());
        dto.setHasExpired(message.getExpiredTime() < System.currentTimeMillis());
        return dto;
    }

    private List<MembershipIconType> list(String islandId, List<SubscribeMembershipMessage> messageList) {
        List<MembershipIconType> iconTypeList = new ArrayList<>();
        iconTypeList.add(MembershipIconType.FEED);

        BoxMessage boxMessage = this.boxService.retrieveBoxInfo(islandId);
        ProtocolStringList boxMembershipIdList = boxMessage.getMembershipIdsList();
        for (SubscribeMembershipMessage message : messageList) {
            if (boxMessage.getEnabled() && (boxMembershipIdList.size() == 0 || boxMembershipIdList.contains(message.getMembershipId())) && message.getExpiredTime() > System.currentTimeMillis()) {
                iconTypeList.add(MembershipIconType.QUESTION);
                break;
            }
        }

        IslandChatgroupsResponse response = this.chatService.retrieveChatgroupsByIslandId(islandId, messageList.get(0).getUserId(), 0, 10);
        if (response.getChatgroupsList().size() > 0) {
            iconTypeList.add(MembershipIconType.GROUP);
        }

        iconTypeList.add(MembershipIconType.MESSAGE);
        return iconTypeList;
    }
}
