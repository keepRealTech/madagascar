package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.asity.ChatAccessMessage;
import org.springframework.stereotype.Component;
import swagger.model.BriefUserDTO;
import swagger.model.ChatAccessDTO;
import swagger.model.ChatTokenDTO;
import swagger.model.IslandChatAccessDTO;

import java.util.List;

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
        IslandChatAccessDTO islandChatAccessDTO = new IslandChatAccessDTO();
        islandChatAccessDTO.setId(chatAccessMessage.getId());
        islandChatAccessDTO.setIslandId(chatAccessMessage.getIslandId());
        islandChatAccessDTO.setMemberCount(memberCount);
        islandChatAccessDTO.setGroupchatCount(chatGroupCount);
        islandChatAccessDTO.setRecentMembers(recentMembers);
        return islandChatAccessDTO;
    }

}