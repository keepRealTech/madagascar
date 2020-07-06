package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.service.ChatService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ChatApi;
import swagger.model.ChatTokenDTO;
import swagger.model.ChatTokenResponse;

import java.util.Objects;

/**
 * Represents the chat controller.
 */
@RestController
public class ChatController implements ChatApi {

    private final UserService userService;
    private final ChatService chatService;

    /**
     * Constructs the chat controller.
     *
     * @param userService {@link UserService}.
     * @param chatService {@link ChatService}.
     */
    public ChatController(UserService userService,
                          ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    /**
     * Implements the user register to rongyun api.
     *
     * @return {@link ChatTokenResponse}.
     */
    @Cacheable(value = "posterFeedDTO", keyGenerator = "UserIdKeyGenerator")
    public ResponseEntity<ChatTokenResponse> apiV1ChatsTokenGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        UserMessage userMessage = this.userService.retrieveUserById(userId);

        if (Objects.isNull(userMessage)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_NOT_FOUND_ERROR);
        }

        String token = this.chatService.registerUser(userMessage);
        ChatTokenDTO chatTokenDTO = new ChatTokenDTO();
        chatTokenDTO.setToken(token);

        ChatTokenResponse response = new ChatTokenResponse();
        response.data(chatTokenDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
