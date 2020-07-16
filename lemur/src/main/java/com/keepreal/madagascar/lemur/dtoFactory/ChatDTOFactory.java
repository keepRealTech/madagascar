package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.asity.ChatAccessMessage;
import com.keepreal.madagascar.asity.ChatgroupMessage;
import org.springframework.stereotype.Component;
import swagger.model.BriefIslandDTO;
import swagger.model.BriefUserDTO;
import swagger.model.ChatAccessDTO;
import swagger.model.ChatGroupDTO;
import swagger.model.ChatTokenDTO;
import swagger.model.IslandChatAccessDTO;
import swagger.model.IslandGroupedChatGroupDTO;
import swagger.model.SimpleMembershipDTO;
import swagger.model.UserChatGroupDTO;

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
     * @param userDTO   {@link BriefUserDTO}.
     * @param hasAccess Whether has access.
     * @return {@link ChatAccessDTO}.
     */
    public ChatAccessDTO buildAccess(BriefUserDTO userDTO, Boolean hasAccess) {
        ChatAccessDTO chatAccessDTO = new ChatAccessDTO();
        chatAccessDTO.setUser(userDTO);
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
     * Creates {@link ChatGroupDTO}.
     *
     * @param chatgroup      {@link ChatgroupMessage}.
     * @param membershipDTOS {@link SimpleMembershipDTO}.
     * @return {@link ChatGroupDTO}.
     */
    public ChatGroupDTO valueOf(ChatgroupMessage chatgroup, List<SimpleMembershipDTO> membershipDTOS) {
        if (Objects.isNull(chatgroup)) {
            return null;
        }

        ChatGroupDTO chatGroupDTO = new ChatGroupDTO();
        chatGroupDTO.setId(chatgroup.getId());
        chatGroupDTO.setIslandId(chatgroup.getIslandId());
        chatGroupDTO.setName(chatgroup.getName());
        chatGroupDTO.setBulletin(chatgroup.getBulletin());
        chatGroupDTO.setMemberCount(chatgroup.getMemberCount());
        chatGroupDTO.setMemberships(membershipDTOS);
        chatGroupDTO.setIsMuted(chatgroup.getMuted());

        return chatGroupDTO;
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
     * Creates the {@link UserChatGroupDTO}.
     *
     * @param chatgroup         {@link ChatgroupMessage}.
     * @param membershipDTOS    {@link SimpleMembershipDTO}.
     * @param userMembershipIds User membership ids.
     * @param userId            User id.
     * @return {@link UserChatGroupDTO}.
     */
    public UserChatGroupDTO valueOf(ChatgroupMessage chatgroup, List<SimpleMembershipDTO> membershipDTOS, List<String> userMembershipIds, String userId) {
        if (Objects.isNull(chatgroup)) {
            return null;
        }

        UserChatGroupDTO userChatGroupDTO = new UserChatGroupDTO();
        userChatGroupDTO.setId(chatgroup.getId());
        userChatGroupDTO.setIslandId(chatgroup.getIslandId());
        userChatGroupDTO.setName(chatgroup.getName());
        userChatGroupDTO.setBulletin(chatgroup.getBulletin());
        userChatGroupDTO.setMemberCount(chatgroup.getMemberCount());
        userChatGroupDTO.setMemberships(membershipDTOS);
        userChatGroupDTO.setIsMuted(chatgroup.getMuted());

        Set<String> groupMembershipIds = membershipDTOS.stream()
                .map(SimpleMembershipDTO::getId)
                .collect(Collectors.toSet());

        userChatGroupDTO.hasAccess(chatgroup.getHostId().equals(userId)
                || groupMembershipIds.isEmpty()
                || groupMembershipIds.stream().anyMatch(userMembershipIds::contains));

        return userChatGroupDTO;
    }

}