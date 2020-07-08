package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.asity.ChatAccessMessage;
import com.keepreal.madagascar.asity.ChatgroupMessage;
import com.keepreal.madagascar.coua.Membership;
import org.springframework.stereotype.Component;
import swagger.model.BriefUserDTO;
import swagger.model.ChatAccessDTO;
import swagger.model.ChatGroupDTO;
import swagger.model.ChatTokenDTO;
import swagger.model.IslandChatAccessDTO;
import swagger.model.MembershipDTO;
import swagger.model.SimpleMembershipDTO;

import java.util.List;
import java.util.Objects;

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
        return islandChatAccessDTO;
    }


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

        return chatGroupDTO;
    }

}