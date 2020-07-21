package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.asity.ChatAccessMessage;
import com.keepreal.madagascar.asity.ChatgroupMessage;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.UserService;
import org.springframework.stereotype.Component;
import swagger.model.BriefIslandDTO;
import swagger.model.BriefUserDTO;
import swagger.model.ChatAccessDTO;
import swagger.model.ChatGroupDTO;
import swagger.model.ChatTokenDTO;
import swagger.model.IslandChatAccessDTO;
import swagger.model.IslandGroupedChatGroupDTO;
import swagger.model.SimpleMembershipDTO;
import swagger.model.UserDTO;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the chat dto factory.
 */
@Component
public class ChatDTOFactory {

    private final UserService userService;
    private final UserDTOFactory userDTOFactory;
    private final IslandService islandService;
    private final IslandDTOFactory islandDTOFactory;

    /**
     * Constructs the chat dto factory.
     *
     * @param userService      {@link UserService}.
     * @param userDTOFactory   {@link UserDTOFactory}.
     * @param islandService    {@link IslandService}.
     * @param islandDTOFactory {@link IslandDTOFactory}.
     */
    public ChatDTOFactory(UserService userService,
                          UserDTOFactory userDTOFactory,
                          IslandService islandService,
                          IslandDTOFactory islandDTOFactory) {
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
        this.islandService = islandService;
        this.islandDTOFactory = islandDTOFactory;
    }

    /**
     * Builds the chat token dto.
     *
     * @param token Token.
     * @return {@link ChatTokenDTO}.
     */
    public ChatTokenDTO buildToken(String token) {
        ChatTokenDTO chatTokenDTO = new ChatTokenDTO();
        chatTokenDTO.setToken(token);
        return chatTokenDTO;
    }

    /**
     * Builds the private chat access dto.
     *
     * @param userDTO          {@link UserDTO}.
     * @param createdIslandIds Created island ids.
     * @param hasAccess        Whether has access.
     * @return {@link ChatAccessDTO}.
     */
    public ChatAccessDTO buildAccess(UserDTO userDTO, List<String> createdIslandIds, Boolean hasAccess) {
        ChatAccessDTO chatAccessDTO = new ChatAccessDTO();
        chatAccessDTO.setUser(userDTO);
        chatAccessDTO.setCreatedIslandIds(createdIslandIds);
        chatAccessDTO.setHasAccess(hasAccess);
        return chatAccessDTO;
    }

    /**
     * Builds island access dto.
     *
     * @param chatAccessMessage {@link ChatAccessMessage}.
     * @param memberCount       Enabled member count.
     * @param chatGroupCount    Existing chat group count.
     * @param recentMembers     {@link BriefUserDTO}.
     * @return {@link IslandChatAccessDTO}.
     */
    public IslandChatAccessDTO buildIslandAccess(ChatAccessMessage chatAccessMessage, Integer memberCount,
                                                 Integer chatGroupCount, List<BriefUserDTO> recentMembers) {
        if (Objects.isNull(chatAccessMessage)) {
            return null;
        }

        IslandChatAccessDTO islandChatAccessDTO = new IslandChatAccessDTO();
        islandChatAccessDTO.setId(chatAccessMessage.getId());
        islandChatAccessDTO.setIslandId(chatAccessMessage.getIslandId());
        islandChatAccessDTO.setMemberCount(memberCount);
        islandChatAccessDTO.setGroupchatCount(chatGroupCount);
        islandChatAccessDTO.setRecentMembers(recentMembers);
        islandChatAccessDTO.setHasAccess(chatAccessMessage.getHasAccess());
        return islandChatAccessDTO;
    }

    /**
     * Creates the {@link IslandGroupedChatGroupDTO}.
     *
     * @param briefIslandDTO          {@link BriefIslandDTO}.
     * @param chatgroupMessageListMap {@link ChatgroupMessage} as key, {@link SimpleMembershipDTO} as value.
     * @param userMembershipIds       User membership ids.
     * @param userId                  User id.
     * @return {@link IslandGroupedChatGroupDTO}.
     */
    public IslandGroupedChatGroupDTO valueOf(BriefIslandDTO briefIslandDTO,
                                             Map<ChatgroupMessage, List<SimpleMembershipDTO>> chatgroupMessageListMap,
                                             List<String> userMembershipIds,
                                             String userId) {
        if (Objects.isNull(briefIslandDTO)) {
            return null;
        }

        IslandGroupedChatGroupDTO islandGroupedChatGroupDTO = new IslandGroupedChatGroupDTO();
        islandGroupedChatGroupDTO.setIsland(briefIslandDTO);
        islandGroupedChatGroupDTO.setChatgroups(chatgroupMessageListMap.entrySet().stream().map(
                entry -> this.valueOf(entry.getKey(), entry.getValue(), userMembershipIds, userId)).collect(Collectors.toList()));

        return islandGroupedChatGroupDTO;
    }

    /**
     * Creates the {@link ChatGroupDTO}.
     *
     * @param chatgroup         {@link ChatgroupMessage}.
     * @param membershipDTOS    {@link SimpleMembershipDTO}.
     * @param userMembershipIds User membership ids.
     * @param userId            User id.
     * @return {@link ChatGroupDTO}.
     */
    public ChatGroupDTO valueOf(ChatgroupMessage chatgroup, List<SimpleMembershipDTO> membershipDTOS, List<String> userMembershipIds, String userId) {
        if (Objects.isNull(chatgroup)) {
            return null;
        }

        ChatGroupDTO userChatGroupDTO = new ChatGroupDTO();
        userChatGroupDTO.setId(chatgroup.getId());
        userChatGroupDTO.setName(chatgroup.getName());
        userChatGroupDTO.setBulletin(chatgroup.getBulletin());
        userChatGroupDTO.setMemberCount(chatgroup.getMemberCount());
        userChatGroupDTO.setMemberships(membershipDTOS);
        userChatGroupDTO.setIsMuted(chatgroup.getMuted());
        userChatGroupDTO.setHasJoined(chatgroup.getJoined());

        userChatGroupDTO.setHost(this.userDTOFactory.briefValueOf(this.userService.retrieveUserById(chatgroup.getHostId())));
        userChatGroupDTO.setIsland(this.islandDTOFactory.briefValueOf(this.islandService.retrieveIslandById(chatgroup.getIslandId())));

        Set<String> groupMembershipIds = membershipDTOS.stream()
                .map(SimpleMembershipDTO::getId)
                .collect(Collectors.toSet());

        userChatGroupDTO.setHasAccess(chatgroup.getHostId().equals(userId)
                || groupMembershipIds.isEmpty()
                || groupMembershipIds.stream().anyMatch(userMembershipIds::contains));

        return userChatGroupDTO;
    }

}