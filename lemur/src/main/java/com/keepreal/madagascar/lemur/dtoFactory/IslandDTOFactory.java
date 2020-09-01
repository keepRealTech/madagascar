package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.coua.DiscoverIslandMessage;
import com.keepreal.madagascar.coua.IslandIdentityMessage;
import com.keepreal.madagascar.coua.IslandProfileResponse;
import com.keepreal.madagascar.lemur.config.GeneralConfiguration;
import com.keepreal.madagascar.lemur.service.ChatService;
import com.keepreal.madagascar.lemur.service.FeedService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.RepostService;
import com.keepreal.madagascar.lemur.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import swagger.model.BriefIslandDTO;
import swagger.model.FullIslandDTO;
import swagger.model.HostIntroductionDTO;
import swagger.model.IntroPrerequestsDTO;
import swagger.model.IntroductionDTO;
import swagger.model.IslandAccessType;
import swagger.model.IslandDTO;
import swagger.model.IslandIdentityDTO;
import swagger.model.IslandProfileDTO;
import swagger.model.RecommendIslandDTO;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the island dto factory.
 */
@Service
public class IslandDTOFactory {

    private static final int DEFAULT_OFFICIAL_ISLAND_MEMBER_COUNT = 99_999_999;
    private static final String SUBSCRIBER_INTRODUCTION_TITLE = "欢迎加入我的岛！在这里你可以：";
    private static final String SUBSCRIBER_INTRODUCTION_CONTENT =
                    "1.支持我更好创作\r\n" +
                    "2.支持后享受专属权益\r\n" +
                    "3.最快看到我的动态，或向我提问";
    private static final String HOST_INTRODUCTION_TITLE = "你成为了一名创作者！";
    private static final String HOST_INTRODUCTION_CONTENT = "现在去分享你的创作主页来获得粉丝的支持吧！";

    private final ChatService chatService;
    private final RepostService repostService;
    private final FeedService feedService;
    private final MembershipService membershipService;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;
    private final GeneralConfiguration generalConfiguration;

    /**
     * Constructs the island dto factory.
     *
     * @param chatService          {@link ChatService}.
     * @param repostService        {@link RepostService}.
     * @param feedService          {@link FeedService}.
     * @param membershipService    {@link MembershipService}.
     * @param userService          {@link UserService}.
     * @param userDTOFactory       {@link UserDTOFactory}.
     * @param generalConfiguration {@link GeneralConfiguration}.
     */
    public IslandDTOFactory(ChatService chatService,
                            RepostService repostService,
                            FeedService feedService,
                            MembershipService membershipService,
                            UserService userService,
                            UserDTOFactory userDTOFactory,
                            GeneralConfiguration generalConfiguration) {
        this.chatService = chatService;
        this.repostService = repostService;
        this.feedService = feedService;
        this.membershipService = membershipService;
        this.userService = userService;
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
        islandDTO.setAccessType(this.convertAccessType(island.getIslandAccessType()));

        islandDTO.setHost(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(island.getHostId())));

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
        briefIslandDTO.setAccessType(this.convertAccessType(island.getIslandAccessType()));

        return briefIslandDTO;
    }

    /**
     * Converts the {@link DiscoverIslandMessage} into {@link RecommendIslandDTO}.
     *
     * @param discoverIsland {@link DiscoverIslandMessage}.
     * @return {@link RecommendIslandDTO}.
     */
    public RecommendIslandDTO valueOf(DiscoverIslandMessage discoverIsland) {
        if (Objects.isNull(discoverIsland)) {
            return null;
        }

        RecommendIslandDTO recommendIslandDTO = new RecommendIslandDTO();
        recommendIslandDTO.setId(discoverIsland.getIsland().getId());
        recommendIslandDTO.setName(discoverIsland.getIsland().getName());
        recommendIslandDTO.setDescription(discoverIsland.getIsland().getDescription());
        recommendIslandDTO.setHostId(discoverIsland.getIsland().getHostId());
        recommendIslandDTO.setPortraitImageUri(discoverIsland.getIsland().getPortraitImageUri());
        recommendIslandDTO.setAccessType(this.convertAccessType(discoverIsland.getIsland().getIslandAccessType()));

        recommendIslandDTO.setRecommendation(discoverIsland.getRecommendation());
        recommendIslandDTO.setHost(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(discoverIsland.getIsland().getHostId())));

        return recommendIslandDTO;
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
        fullIslandDTO.setAccessType(this.convertAccessType(island.getIslandAccessType()));

        fullIslandDTO.setHost(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(island.getHostId())));

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

        // Deprecated fields
        islandProfileDTO.setHostIntroduction(this.buildHostIntroduction());
        islandProfileDTO.setSubscriberIntroduction(this.buildSubscriberIntroduction(false));

        if (userId.equals(islandProfileDTO.getHost().getId())) {
            islandProfileDTO.setIntroduction(this.buildHostIntroduction(islandProfileResponse.getHostShouldIntroduce()));
        } else if (this.generalConfiguration.getOfficialIslandIdList().contains(islandProfileResponse.getIsland().getId())) {
            islandProfileDTO.setIntroduction(this.buildSubscriberIntroduction(false));
        } else {
            islandProfileDTO.setIntroduction(this.buildSubscriberIntroduction(islandProfileResponse.getUserShouldIntroduce()));
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
     * @return {@link IntroductionDTO}
     */
    private IntroductionDTO buildSubscriberIntroduction(Boolean shouldIntroduce) {
        IntroductionDTO subscriberIntroductionDTO = new IntroductionDTO();

        if (Objects.isNull(shouldIntroduce) || !shouldIntroduce) {
            subscriberIntroductionDTO.setShouldPopup(false);
            return subscriberIntroductionDTO;
        }

        subscriberIntroductionDTO.setShouldPopup(true);
        subscriberIntroductionDTO.setTitle(IslandDTOFactory.SUBSCRIBER_INTRODUCTION_TITLE);
        subscriberIntroductionDTO.setContent(IslandDTOFactory.SUBSCRIBER_INTRODUCTION_CONTENT);

        return subscriberIntroductionDTO;
    }

    /**
     * Builds the host introduction dto.
     *
     * @param shouldIntroduce Whether should pip up the intro.
     * @return {@link IntroductionDTO}.
     */
    private IntroductionDTO buildHostIntroduction(Boolean shouldIntroduce) {
        IntroductionDTO hostIntroductionDTO = new IntroductionDTO();

        if (Objects.isNull(shouldIntroduce) || !shouldIntroduce) {
            hostIntroductionDTO.setShouldPopup(false);
            return hostIntroductionDTO;
        }

        hostIntroductionDTO.setShouldPopup(true);
        hostIntroductionDTO.setTitle(IslandDTOFactory.HOST_INTRODUCTION_TITLE);
        hostIntroductionDTO.setContent(IslandDTOFactory.HOST_INTRODUCTION_CONTENT);

        return hostIntroductionDTO;
    }

    /**
     * Builds the host introduction dto.
     *
     * @return {@link HostIntroductionDTO}.
     */
    @Deprecated
    private HostIntroductionDTO buildHostIntroduction() {
        HostIntroductionDTO hostIntroductionDTO = new HostIntroductionDTO();
        IntroPrerequestsDTO introPrerequestsDTO = new IntroPrerequestsDTO();
        introPrerequestsDTO.setHasFeeds(true);
        introPrerequestsDTO.setHasMemberships(true);
        introPrerequestsDTO.setHasReposts(true);
        hostIntroductionDTO.setShouldPopup(false);
        hostIntroductionDTO.setPres(introPrerequestsDTO);
        return hostIntroductionDTO;
    }

    /**
     * Converts {@link com.keepreal.madagascar.common.IslandAccessType} to {@link IslandAccessType}.
     *
     * @param islandAccessType {@link com.keepreal.madagascar.common.IslandAccessType}.
     * @return {@link IslandAccessType}.
     */
    private IslandAccessType convertAccessType(com.keepreal.madagascar.common.IslandAccessType islandAccessType) {
        if (Objects.isNull(islandAccessType)) {
            return IslandAccessType.PUBLIC;
        }

        switch (islandAccessType) {
            case ISLAND_ACCESS_PRIVATE:
                return IslandAccessType.PRIVATE;
            case ISLAND_ACCESS_PUBLIC:
            default:
                return IslandAccessType.PUBLIC;
        }
    }

}
