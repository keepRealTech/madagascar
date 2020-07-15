package com.keepreal.madagascar.lemur.controller;

import com.keepreal.madagascar.asity.ChatgroupMessage;
import com.keepreal.madagascar.asity.IslandChatgroupsResponse;
import com.keepreal.madagascar.asity.UserChatgroupsResponse;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.lemur.dtoFactory.ChatDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.IslandDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.MembershipDTOFactory;
import com.keepreal.madagascar.lemur.dtoFactory.UserDTOFactory;
import com.keepreal.madagascar.lemur.service.ChatService;
import com.keepreal.madagascar.lemur.service.IslandService;
import com.keepreal.madagascar.lemur.service.MembershipService;
import com.keepreal.madagascar.lemur.service.SubscribeMembershipService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.ChatApi;
import swagger.model.ChatAccessResponse;
import swagger.model.ChatGroupResponse;
import swagger.model.ChatTokenResponse;
import swagger.model.DummyResponse;
import swagger.model.IslandChatAccessResponse;
import swagger.model.IslandChatGroupsResponse;
import swagger.model.PostChatGroupRequest;
import swagger.model.PutChatGroupRequest;
import swagger.model.SimpleMembershipDTO;
import swagger.model.UserChatGroupsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the chat controller.
 */
@RestController
public class ChatController implements ChatApi {

    private final IslandService islandService;
    private final UserService userService;
    private final ChatService chatService;
    private final MembershipService membershipService;
    private final SubscribeMembershipService subscribeMembershipService;

    private final ChatDTOFactory chatDTOFactory;
    private final UserDTOFactory userDTOFactory;
    private final MembershipDTOFactory membershipDTOFactory;
    private final IslandDTOFactory islandDTOFactory;

    /**
     * Constructs the chat controller.
     *
     * @param islandService              {@link IslandService}.
     * @param userService                {@link UserService}.
     * @param chatService                {@link ChatService}.
     * @param membershipService          {@link MembershipService}.
     * @param subscribeMembershipService {@link SubscribeMembershipService}.
     * @param chatDTOFactory             {@link ChatDTOFactory}.
     * @param userDTOFactory             {@link UserDTOFactory}.
     * @param membershipDTOFactory       {@link MembershipDTOFactory}.
     * @param islandDTOFactory           {@link IslandDTOFactory}.
     */
    public ChatController(IslandService islandService,
                          UserService userService,
                          ChatService chatService,
                          MembershipService membershipService,
                          SubscribeMembershipService subscribeMembershipService,
                          ChatDTOFactory chatDTOFactory,
                          UserDTOFactory userDTOFactory,
                          MembershipDTOFactory membershipDTOFactory,
                          IslandDTOFactory islandDTOFactory) {
        this.islandService = islandService;
        this.userService = userService;
        this.chatService = chatService;
        this.membershipService = membershipService;
        this.subscribeMembershipService = subscribeMembershipService;
        this.chatDTOFactory = chatDTOFactory;
        this.userDTOFactory = userDTOFactory;
        this.membershipDTOFactory = membershipDTOFactory;
        this.islandDTOFactory = islandDTOFactory;
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

    /**
     * Implements the island chat access get api.
     *
     * @param id Island id.
     * @return {@link IslandChatAccessResponse}.
     */
    @Override
    public ResponseEntity<IslandChatAccessResponse> apiV1IslandsIdChataccessGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        boolean subscribed = this.islandService.retrieveIslandSubscribeStateByUserId(userId, Collections.singletonList(id)).get(id);

        if (!subscribed) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_ISLAND_NOT_SUBSCRIBED_ERROR);
        }

        com.keepreal.madagascar.asity.IslandChatAccessResponse islandChatAccessResponse =
                this.chatService.retrieveChatAccessByIslandId(id);

        IslandChatAccessResponse response = new IslandChatAccessResponse();
        response.data(this.chatDTOFactory.buildIslandAccess(islandChatAccessResponse.getChatAccess(),
                islandChatAccessResponse.getEnabledMemberCount(),
                islandChatAccessResponse.getIslandChatGroupCount(),
                islandChatAccessResponse.getRecentEnabledUserIdsList().stream()
                        .map(memberId -> {
                            UserMessage userMessage = this.userService.retrieveUserById(memberId);
                            return this.userDTOFactory.briefValueOf(userMessage);
                        }).collect(Collectors.toList())));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the enable chat access post api.
     *
     * @param id id (required) Island id.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1IslandsIdChataccessGrantPost(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        IslandMessage islandMessage = this.islandService.retrieveIslandById(id);

        if (!islandMessage.getHostId().equals(userId)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_FORBIDDEN);
        }

        this.chatService.enableChatAccess(id);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the island chat group create api.
     *
     * @param id                   id (required) Island id.
     * @param postChatGroupRequest (required) {@link PostChatGroupRequest}.
     * @return {@link ChatGroupResponse}.
     */
    @Override
    public ResponseEntity<ChatGroupResponse> apiV1IslandsIdChatgroupsPost(String id, PostChatGroupRequest postChatGroupRequest) {
        IslandMessage island = this.islandService.retrieveIslandById(id);

        if (Objects.isNull(island)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_ISLAND_NOT_FOUND_ERROR);
        }

        String userId = HttpContextUtils.getUserIdFromContext();

        if (!island.getHostId().equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<MembershipMessage> membershipMessageList = new ArrayList<>();
        if (Objects.nonNull(postChatGroupRequest.getMembershipIds())
                && !postChatGroupRequest.getMembershipIds().isEmpty()) {
            membershipMessageList = this.membershipService.retrieveMembershipsByIslandId(id).stream()
                    .filter(membership -> postChatGroupRequest.getMembershipIds().contains(membership.getId()))
                    .collect(Collectors.toList());
            if (membershipMessageList.size() != postChatGroupRequest.getMembershipIds().size()) {
                throw new KeepRealBusinessException(ErrorCode.REQUEST_MEMBERSHIP_NOT_FOUNT_ERROR);
            }
        }

        ChatgroupMessage chatgroupMessage = this.chatService.createChatgroup(id, userId, postChatGroupRequest.getName(),
                postChatGroupRequest.getMembershipIds(), postChatGroupRequest.getBulletin());

        List<SimpleMembershipDTO> membershipDTOList = membershipMessageList.stream()
                .map(this.membershipDTOFactory::simpleValueOf)
                .collect(Collectors.toList());

        ChatGroupResponse response = new ChatGroupResponse();
        response.setData(this.chatDTOFactory.valueOf(chatgroupMessage, membershipDTOList));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the dismiss chat group api.
     *
     * @param id id (required) Chat group id.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1ChatgroupsIdDelete(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        this.chatService.dismissChatgroup(id, userId);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the chat group update api.
     *
     * @param id                  id (required) Chat group id.
     * @param putChatGroupRequest (required) {@link PutChatGroupRequest}.
     * @return {@link ChatGroupResponse}.
     */
    @Override
    public ResponseEntity<ChatGroupResponse> apiV1ChatgroupsIdPut(String id, PutChatGroupRequest putChatGroupRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();

        ChatgroupMessage chatgroupMessage = this.chatService.retrieveChatgroupById(id, userId);
        if (Objects.isNull(chatgroupMessage)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_CHATGROUP_NOT_FOUND_ERROR);
        }

        List<MembershipMessage> membershipMessageList = new ArrayList<>();
        if (Objects.isNull(putChatGroupRequest.getMembershipIds())) {
            ChatgroupMessage finalChatgroupMessage = chatgroupMessage;
            membershipMessageList = this.membershipService.retrieveMembershipsByIslandId(chatgroupMessage.getIslandId()).stream()
                    .filter(membership -> finalChatgroupMessage.getMembershipIdsList().contains(membership.getId()))
                    .collect(Collectors.toList());
        } else if (!putChatGroupRequest.getMembershipIds().isEmpty()) {
            membershipMessageList = this.membershipService.retrieveMembershipsByIslandId(chatgroupMessage.getIslandId()).stream()
                    .filter(membership -> putChatGroupRequest.getMembershipIds().contains(membership.getId()))
                    .collect(Collectors.toList());
            if (membershipMessageList.size() != putChatGroupRequest.getMembershipIds().size()) {
                throw new KeepRealBusinessException(ErrorCode.REQUEST_MEMBERSHIP_NOT_FOUNT_ERROR);
            }
        }

        chatgroupMessage = this.chatService.updateChatgroup(id,
                userId,
                putChatGroupRequest.getName(),
                Objects.nonNull(putChatGroupRequest.getMembershipIds()) ? putChatGroupRequest.getMembershipIds() : chatgroupMessage.getMembershipIdsList(),
                putChatGroupRequest.getBulletin(),
                putChatGroupRequest.getIsMuted());

        List<SimpleMembershipDTO> membershipDTOList = membershipMessageList.stream()
                .map(this.membershipDTOFactory::simpleValueOf)
                .collect(Collectors.toList());

        ChatGroupResponse response = new ChatGroupResponse();
        response.setData(this.chatDTOFactory.valueOf(chatgroupMessage, membershipDTOList));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the join chat group api.
     *
     * @param id id (required) Groupchat id.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1ChatgroupsIdJoinPost(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        ChatgroupMessage chatgroupMessage = this.chatService.retrieveChatgroupById(id, userId);
        List<String> userMembershipIds = this.subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(id, userId);

        if (!chatgroupMessage.getMembershipIdsList().isEmpty()
                && chatgroupMessage.getMembershipIdsList().stream().noneMatch(userMembershipIds::contains)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        this.chatService.joinChatgroup(id, userId);

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get island chat groups api.
     *
     * @param id       id (required) Island id.
     * @param page     page number (optional, default to 0) Page index.
     * @param pageSize size of a page (optional, default to 10) Page size.
     * @return {@link IslandChatGroupsResponse}.
     */
    @Override
    public ResponseEntity<IslandChatGroupsResponse> apiV1IslandsIdChatgroupsGet(String id,
                                                                                Integer page,
                                                                                Integer pageSize) {
        String userId = HttpContextUtils.getUserIdFromContext();

        IslandChatgroupsResponse chatgroupsResponse = this.chatService.retrieveChatgroupsByIslandId(id, userId, page, pageSize);

        Map<String, MembershipMessage> membershipMap = this.membershipService.retrieveMembershipsByIslandId(id).stream()
                .collect(Collectors.toMap(MembershipMessage::getId, Function.identity(), (mem1, mem2) -> mem1, HashMap::new));

        List<String> userMembershipIds = this.subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(id, userId);

        IslandChatGroupsResponse response = new IslandChatGroupsResponse();
        response.setData(chatgroupsResponse.getChatgroupsList()
                .stream()
                .map(chatgroupMessage -> {
                    List<SimpleMembershipDTO> membershipDTOList = chatgroupMessage.getMembershipIdsList().stream()
                            .map(membershipId -> membershipMap.getOrDefault(membershipId, null))
                            .filter(Objects::nonNull)
                            .map(this.membershipDTOFactory::simpleValueOf)
                            .collect(Collectors.toList());
                    return this.chatDTOFactory.valueOf(chatgroupMessage, membershipDTOList, userMembershipIds);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        response.setPageInfo(PaginationUtils.getPageInfo(chatgroupsResponse.getPageResponse()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves a chatgroup by id.
     *
     * @param id id (required) Chatgroup id.
     * @return {@link ChatGroupResponse}.
     */
    @Override
    public ResponseEntity<ChatGroupResponse> apiV1ChatgroupsIdGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        ChatgroupMessage chatgroupMessage = this.chatService.retrieveChatgroupById(id, userId);

        if (Objects.isNull(chatgroupMessage)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_CHATGROUP_NOT_FOUND_ERROR);
        }

        List<MembershipMessage> membershipMessageList = new ArrayList<>();

        if (!chatgroupMessage.getMembershipIdsList().isEmpty()) {
            membershipMessageList = this.membershipService.retrieveMembershipsByIslandId(chatgroupMessage.getIslandId()).stream()
                    .filter(membership -> chatgroupMessage.getMembershipIdsList().contains(membership.getId()))
                    .collect(Collectors.toList());
        }

        List<SimpleMembershipDTO> membershipDTOList = membershipMessageList.stream()
                .map(this.membershipDTOFactory::simpleValueOf)
                .collect(Collectors.toList());

        ChatGroupResponse response = new ChatGroupResponse();
        response.setData(this.chatDTOFactory.valueOf(chatgroupMessage, membershipDTOList));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the get user chat groups.
     *
     * @param id id (required) User id.
     * @return {@link UserChatGroupsResponse}.
     */
    @Override
    public ResponseEntity<UserChatGroupsResponse> apiV1UsersIdChatgroupsGet(String id) {
        String userId = HttpContextUtils.getUserIdFromContext();

        if (!userId.equals(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        UserChatgroupsResponse userChatgroupsResponse = this.chatService.retrieveChatgroupsByUserId(userId);
        // Island id:chatgroup map
        Map<String, List<ChatgroupMessage>> chatgroupMap = userChatgroupsResponse.getChatgroupsList()
                .stream()
                .collect(Collectors.groupingBy(ChatgroupMessage::getIslandId, HashMap::new, Collectors.toList()));
        // Membership id:membership map
        Map<String, MembershipMessage> membershipMap = this.membershipService.retrieveMembershipsByIslandIds(chatgroupMap.keySet())
                .stream()
                .collect(Collectors.toMap(MembershipMessage::getId, Function.identity(), (mem1, mem2) -> mem1, HashMap::new));
        // User membership id list
        List<String> userMembershipIds = this.subscribeMembershipService.retrieveSubscribedMembershipsByIslandIdAndUserId(null, userId);

        // Island id:(chatgroup:chatgroup required membershipDTO list) map
        Map<String, Map<ChatgroupMessage, List<SimpleMembershipDTO>>> islandMembershipMap =
                chatgroupMap.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                entry -> entry.getValue().stream().collect(Collectors.toMap(
                                        Function.identity(),
                                        chatgroup -> chatgroup.getMembershipIdsList()
                                                .stream()
                                                .map(membershipId -> this.membershipDTOFactory.simpleValueOf(membershipMap.getOrDefault(membershipId, null)))
                                                .collect(Collectors.toList())
                                ))));

        UserChatGroupsResponse response = new UserChatGroupsResponse();
        response.setData(islandMembershipMap.entrySet().stream().map(entry ->
                this.chatDTOFactory.valueOf(
                        this.islandDTOFactory.briefValueOf(this.islandService.retrieveIslandById(entry.getKey())),
                        entry.getValue(),
                        userMembershipIds)).collect(Collectors.toList()));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
