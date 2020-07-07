package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.lemur.dtoFactory.ChatDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.UserDTOFactory;
import com.keepreal.madagascar.lemur.service.ChatService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ChatApi;
import swagger.model.ChatAccessResponse;
import swagger.model.ChatTokenResponse;

import java.util.Collections;
import java.util.Objects;

/**
 * Represents the chat controller.
 */
@RestController
public class ChatController implements ChatApi {

    private final IslandService islandService;
    private final UserService userService;
    private final ChatService chatService;

    private final ChatDTOFactory chatDTOFactory;
    private final UserDTOFactory userDTOFactory;

    /**
     * Constructs the chat controller.
     *
     * @param islandService {@link IslandService}.
     * @param userService   {@link UserService}.
     * @param chatService   {@link ChatService}.
     * @param chatDTOFactory {@link ChatDTOFactory}.
     * @param userDTOFactory {@link UserDTOFactory}.
     */
    public ChatController(IslandService islandService,
                          UserService userService,
                          ChatService chatService,
                          ChatDTOFactory chatDTOFactory,
                          UserDTOFactory userDTOFactory) {
        this.islandService = islandService;
        this.userService = userService;
        this.chatService = chatService;
        this.chatDTOFactory = chatDTOFactory;
        this.userDTOFactory = userDTOFactory;
    }

    /**
     * Implements the user register to rongyun api.
     *
     * @return {@link ChatTokenResponse}.
     */
    @Cacheable(value = "posterFeedDTO", keyGenerator = "UserIdKeyGenerator")
    @Override
    public ResponseEntity<ChatTokenResponse> apiV1ChatsTokenGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        UserMessage userMessage = this.userService.retrieveUserById(userId);

        if (Objects.isNull(userMessage)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_NOT_FOUND_ERROR);
        }

        String token = this.chatService.registerUser(userMessage);

        ChatTokenResponse response = new ChatTokenResponse();
        response.data(this.chatDTOFactory.buildToken(token));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the user chat access get api.
     *
     * @param id id (required) Target user id.
     * @return {@link ChatAccessResponse}.
     */
    @Override
    public ResponseEntity<ChatAccessResponse> apiV1UsersIdChatsGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();
        UserMessage userMessage = this.userService.retrieveUserById(id);

        if (Objects.isNull(userMessage)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_NOT_FOUND_ERROR);
        }

        IslandsResponse targetIslandResponse = this.islandService.retrieveIslands(null, id, null, 0, 1);
        IslandMessage targetIsland = targetIslandResponse.getIslandsList().isEmpty() ?
                null : targetIslandResponse.getIslandsList().get(0);

        ChatAccessResponse response = new ChatAccessResponse();
        if (Objects.isNull(targetIsland)
                || this.islandService.retrieveIslandSubscribeStateByUserId(userId,
                Collections.singletonList(targetIsland.getId())).get(targetIsland.getId())) {
            response.setData(this.chatDTOFactory.buildAccess(this.userDTOFactory.briefValueOf(userMessage), true));
        } else {
            response.setData(this.chatDTOFactory.buildAccess(this.userDTOFactory.briefValueOf(userMessage), false));
        }

        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
