package com.keepreal.madagascar.lemur.dtoFactory;

import org.springframework.stereotype.Component;
import swagger.model.BriefUserDTO;
import swagger.model.ChatAccessDTO;
import swagger.model.ChatTokenDTO;

@Component
public class ChatDTOFactory {

    public ChatTokenDTO buildToken(String token) {
        ChatTokenDTO chatTokenDTO = new ChatTokenDTO();
        chatTokenDTO.setToken(token);
        return chatTokenDTO;
    }

    public ChatAccessDTO buildAccess(BriefUserDTO userDTO, Boolean hasAccess) {
        ChatAccessDTO chatAccessDTO = new ChatAccessDTO();
        chatAccessDTO.setUser(userDTO);
        chatAccessDTO.setHasAccess(hasAccess);
        return chatAccessDTO;
    }
}
