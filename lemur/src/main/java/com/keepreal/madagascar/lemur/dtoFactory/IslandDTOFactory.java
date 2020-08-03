package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.coua.IslandIdentityMessage;
import com.keepreal.madagascar.coua.IslandProfileResponse;
import com.keepreal.madagascar.lemur.config.GeneralConfiguration;
import com.keepreal.madagascar.lemur.service.ChatService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import swagger.model.BriefIslandDTO;
import swagger.model.FullIslandDTO;
import swagger.model.HostIntroductionDTO;
import swagger.model.IslandDTO;
import swagger.model.IslandIdentityDTO;
import swagger.model.IslandProfileDTO;
import swagger.model.SubscriberIntroductionDTO;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the island dto factory.
 */
@Service
public class IslandDTOFactory {

    private static final int DEFAULT_OFFICIAL_ISLAND_MEMBER_COUNT = 99_999_999;
    private static final String SUBSCRIBER_INTRODUCTION_CONTENT = "欢迎加入我的岛！在这里你可以：\r\n" +
            "1.最快了解我的动态\r\n" +
            "2.与岛民们畅聊和分享同好\r\n" +
            "3.订阅会员支持我，并获得专属权益";

    private final ChatService chatService;
    private final UserDTOFactory userDTOFactory;
    private final GeneralConfiguration generalConfiguration;

    /**
     * Constructs the island dto factory.
     *
     * @param chatService          {@link ChatService}.
     * @param userDTOFactory       {@link UserDTOFactory}.
     * @param generalConfiguration {@link GeneralConfiguration}.
     */
    public IslandDTOFactory(ChatService chatService,
                            UserDTOFactory userDTOFactory,
                            GeneralConfiguration generalConfiguration) {
        this.chatService = chatService;
        this.userDTOFactory = userDTOFactory;
        this.generalConfiguration = generalConfiguration;
    }

    /**
     * Converts {@link IslandMessage} to {@link IslandDTO}.
     *
     * @param island {@link IslandMessage}.
     * @return {@link IslandDTO}.
     */
    public IslandDTO valueOf(IslandMessage island) {
        if (Objects.isNull(island)) {
            return null;
        }

        IslandDTO islandDTO = new IslandDTO();
        islandDTO.setId(island.getId());
        islandDTO.setName(island.getName());
        int memberCount = island.getMemberCount();
        if (island.getId().equals(generalConfiguration.getSingleOfficialIslandId()))
            memberCount = DEFAULT_OFFICIAL_ISLAND_MEMBER_COUNT;
        islandDTO.setMemberCount(memberCount);
        islandDTO.setDescription(island.getDescription());
        islandDTO.setHostId(island.getHostId());
        islandDTO.setPortraitImageUri(island.getPortraitImageUri());

        return islandDTO;
    }

    /**
     * Converts {@link IslandMessage} to {@link BriefIslandDTO}.
     *
     * @param island {@link IslandMessage}.
     * @return {@link BriefIslandDTO}.
     */
    public BriefIslandDTO briefValueOf(IslandMessage island) {
        if (Objects.isNull(island)) {
            return null;
        }

        BriefIslandDTO briefIslandDTO = new BriefIslandDTO();
        briefIslandDTO.setId(island.getId());
        briefIslandDTO.setName(island.getName());
        briefIslandDTO.setDescription(island.getDescription());
        briefIslandDTO.setHostId(island.getHostId());
        briefIslandDTO.setPortraitImageUri(island.getPortraitImageUri());

        return briefIslandDTO;
    }

    /**
     * Converts {@link IslandMessage} to {@link FullIslandDTO}.
     *
     * @param island     {@link IslandMessage}.
     * @param maskSecret True if should mask secret.
     * @return {@link FullIslandDTO}.
     */
    public FullIslandDTO fullValueOf(IslandMessage island, boolean maskSecret) {
        if (Objects.isNull(island)) {
            return null;
        }

        String secret = island.getSecret();
        FullIslandDTO fullIslandDTO = new FullIslandDTO();
        fullIslandDTO.setId(island.getId());
        fullIslandDTO.setName(island.getName());
        fullIslandDTO.setDescription(island.getDescription());
        fullIslandDTO.setHostId(island.getHostId());
        fullIslandDTO.setPortraitImageUri(island.getPortraitImageUri());
        fullIslandDTO.setSecret(maskSecret ? "******" : secret);
        int memberCount = island.getMemberCount();
        if (island.getId().equals(generalConfiguration.getSingleOfficialIslandId()))
            memberCount = DEFAULT_OFFICIAL_ISLAND_MEMBER_COUNT;
        fullIslandDTO.setMemberCount(memberCount);

        return fullIslandDTO;
    }

    /**
     * Converts {@link IslandProfileResponse} to {@link IslandDTO}.
     *
     * @param islandProfileResponse {@link IslandProfileResponse}.
     * @param userId                User id.
     * @return {@link IslandProfileDTO}.
     */
    public IslandProfileDTO valueOf(IslandProfileResponse islandProfileResponse, String userId) {
        IslandProfileDTO islandProfileDTO = new IslandProfileDTO();

        boolean maskSecret = true;
        if (Objects.nonNull(islandProfileResponse.getIsland())
                && Objects.equals(islandProfileResponse.getIsland().getHostId(), userId)) {
            maskSecret = false;
        }

        islandProfileDTO.setIsland(this.fullValueOf(islandProfileResponse.getIsland(), maskSecret));
        islandProfileDTO.setHost(this.userDTOFactory.valueOf(islandProfileResponse.getHost()));
        islandProfileDTO.setUserIndex(islandProfileResponse.getUserIndex().getValue());
        islandProfileDTO.setSubscribed(!StringUtils.isEmpty(islandProfileDTO.getUserIndex()));
        islandProfileDTO.setSubscribedAt(islandProfileResponse.getSubscribedAt());
        islandProfileDTO.setHasGroupchatAccess(this.chatService.retrieveChatAccessByIslandId(islandProfileResponse
                .getIsland().getId()).getChatAccess().getHasAccess());

        if (userId.equals(islandProfileDTO.getHost().getId())) {
            islandProfileDTO.setHostIntroduction(this.buildHostIntroduction(islandProfileResponse.getShouldIntroduce()));
            islandProfileDTO.setSubscriberIntroduction(this.buildSubscriberIntroduction(false));
        } else {
            islandProfileDTO.setSubscriberIntroduction(this.buildSubscriberIntroduction(islandProfileResponse.getShouldIntroduce()));
            islandProfileDTO.setHostIntroduction(this.buildHostIntroduction(false));
        }

        return islandProfileDTO;
    }

    /**
     * Converts the {@link IslandIdentityMessage} into {@link IslandIdentityDTO}.
     *
     * @param islandIdentityMessage {@link IslandIdentityMessage}.
     * @return {@link IslandIdentityDTO}.
     */
    public IslandIdentityDTO valueOf(IslandIdentityMessage islandIdentityMessage) {
        if (Objects.isNull(islandIdentityMessage)) {
            return null;
        }

        IslandIdentityDTO islandIdentityDTO = new IslandIdentityDTO();
        islandIdentityDTO.setId(islandIdentityMessage.getId());
        islandIdentityDTO.setName(islandIdentityMessage.getName());
        islandIdentityDTO.setDescription(islandIdentityMessage.getDescription());
        islandIdentityDTO.setIconUri(islandIdentityMessage.getIconUri());

        islandIdentityDTO.setColors(Arrays.asList(islandIdentityMessage.getStartColor(), islandIdentityMessage.getEndColor()));
        return islandIdentityDTO;
    }

    /**
     * Builds the subscriber introduction dto.
     *
     * @param shouldIntroduce Whether should pop up the intro.
     * @return {@link SubscriberIntroductionDTO}
     */
    private SubscriberIntroductionDTO buildSubscriberIntroduction(Boolean shouldIntroduce) {
        SubscriberIntroductionDTO subscriberIntroductionDTO = new SubscriberIntroductionDTO();

        if (Objects.isNull(shouldIntroduce) || !shouldIntroduce) {
            subscriberIntroductionDTO.setShouldPopup(false);
            return subscriberIntroductionDTO;
        }

        subscriberIntroductionDTO.setShouldPopup(true);
        subscriberIntroductionDTO.setContent(IslandDTOFactory.SUBSCRIBER_INTRODUCTION_CONTENT);

        return subscriberIntroductionDTO;
    }

    private HostIntroductionDTO buildHostIntroduction(Boolean shouldIntroduce) {
        return null;
    }

}
