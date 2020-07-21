package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.ChatgroupMembersResponse;
import com.keepreal.madagascar.asity.ChatgroupMessage;
import com.keepreal.madagascar.asity.ChatgroupResponse;
import com.keepreal.madagascar.asity.CreateChatgroupRequest;
import com.keepreal.madagascar.asity.DismissChatgroupRequest;
import com.keepreal.madagascar.asity.EnableChatAccessRequest;
import com.keepreal.madagascar.asity.IslandChatAccessResponse;
import com.keepreal.madagascar.asity.IslandChatgroupsResponse;
import com.keepreal.madagascar.asity.JoinChatgroupRequest;
import com.keepreal.madagascar.asity.RegisterRequest;
import com.keepreal.madagascar.asity.RegisterResponse;
import com.keepreal.madagascar.asity.RetreiveChatgroupsByUserIdRequest;
import com.keepreal.madagascar.asity.RetrieveChatAccessByIslandIdRequest;
import com.keepreal.madagascar.asity.RetrieveChatgroupByIdRequest;
import com.keepreal.madagascar.asity.RetrieveChatgroupMembersByGroupIdRequest;
import com.keepreal.madagascar.asity.RetrieveChatgroupsByIslandIdRequest;
import com.keepreal.madagascar.asity.UpdateChatgroupRequest;
import com.keepreal.madagascar.asity.UserChatgroupsResponse;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.util.PaginationUtils;
import com.keepreal.madagascar.vanga.RetrieveMembershipIdsRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the chat service.
 */
@Service
@Slf4j
public class ChatService {

    private static final int NAME_LENGTH_LOWER_THRESHOLD = 2;
    private static final int NAME_LENGTH_UPPER_THRESHOLD = 12;
    private static final int BULLETIN_LENGTH_LOWER_THRESHOLD = 0;
    private static final int BULLETIN_LENGTH_UPPER_THRESHOLD = 500;

    private final Channel channel;

    /**
     * Constructs the chat service.
     *
     * @param channel GRpc managed channel connection to service Asity.
     */
    public ChatService(@Qualifier("asityChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Registers the user to Rongyun and gets a token.
     *
     * @param user {@link UserMessage}.
     * @return Token.
     */
    public String registerUser(UserMessage user) {
        assert Objects.nonNull(user);

        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        RegisterRequest request = RegisterRequest.newBuilder()
                .setUserId(user.getId())
                .setUserName(user.getName())
                .setPortraitUrl(String.format("https://images.keepreal.cn/%s", user.getPortraitImageUri()))
                .build();

        RegisterResponse response;
        try {
            response = stub.registerUser(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Register user chat returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getToken();
    }

    /**
     * Enables the chat access.
     *
     * @param islandId Island id.
     */
    public void enableChatAccess(String islandId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        EnableChatAccessRequest request = EnableChatAccessRequest.newBuilder()
                .setIslandId(islandId)
                .build();

        CommonStatus commonStatus;
        try {
            commonStatus = stub.enableChatAccess(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != commonStatus.getRtn()) {
            throw new KeepRealBusinessException(commonStatus);
        }
    }

    /**
     * Retrieves chat access.
     *
     * @param islandId Island id.
     * @return {@link IslandChatAccessResponse}.
     */
    public IslandChatAccessResponse retrieveChatAccessByIslandId(String islandId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        RetrieveChatAccessByIslandIdRequest request = RetrieveChatAccessByIslandIdRequest.newBuilder()
                .setIslandId(islandId)
                .build();

        IslandChatAccessResponse response;
        try {
            response = stub.retrieveChatAccessByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve chat access returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Creates chat group.
     *
     * @param islandId      Island id.
     * @param userId        User id.
     * @param name          Group name.
     * @param membershipIds Membership ids.
     * @param bulletin      Bulletin.
     * @return {@link ChatgroupMessage}.
     */
    public ChatgroupMessage createChatgroup(String islandId, String userId, String name, List<String> membershipIds, String bulletin) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        CreateChatgroupRequest request = CreateChatgroupRequest.newBuilder()
                .setBulletin(StringUtils.isEmpty(bulletin) ? "" : bulletin)
                .setIslandId(islandId)
                .setName(name)
                .setHostId(userId)
                .addAllMembershipIds(Objects.isNull(membershipIds) ? new ArrayList<>() : membershipIds)
                .build();

        ChatgroupResponse response;
        try {
            response = stub.createChatgroup(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Create chat group returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getChatgroup();
    }

    /**
     * Dismisses a chat group.
     *
     * @param id     Chat group id.
     * @param userId User id.
     */
    public void dismissChatgroup(String id, String userId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        DismissChatgroupRequest request = DismissChatgroupRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        CommonStatus commonStatus;
        try {
            commonStatus = stub.dismissChatgroup(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != commonStatus.getRtn()) {
            throw new KeepRealBusinessException(commonStatus);
        }
    }

    /**
     * Updates a chat group.
     *
     * @param id            Chat group id.
     * @param userId        User id.
     * @param name          Nmae.
     * @param membershipIds Membership id.
     * @param bulletin      Bulletin.
     * @param muted         Muted.
     * @return {@link ChatgroupMessage}.
     */
    public ChatgroupMessage updateChatgroup(String id, String userId, String name, List<String> membershipIds, String bulletin, Boolean muted) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        UpdateChatgroupRequest.Builder requestBuilder = UpdateChatgroupRequest.newBuilder()
                .setId(id)
                .setUserId(userId);

        if (!StringUtils.isEmpty(name)) {
            name = checkLength(name, ChatService.NAME_LENGTH_LOWER_THRESHOLD, ChatService.NAME_LENGTH_UPPER_THRESHOLD);
            requestBuilder.setName(StringValue.of(name));
        }

        if (Objects.nonNull(bulletin)) {
            bulletin = checkLength(bulletin, ChatService.BULLETIN_LENGTH_LOWER_THRESHOLD, ChatService.BULLETIN_LENGTH_UPPER_THRESHOLD);
            requestBuilder.setBulletin(StringValue.of(bulletin));
        }

        if (Objects.nonNull(muted)) {
            requestBuilder.setMuted(BoolValue.of(muted));
        }

        requestBuilder.addAllMembershipIds(membershipIds);

        ChatgroupResponse response;
        try {
            response = stub.updateChatgroup(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Update chat group returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getChatgroup();
    }

    /**
     * Joins the chatgroup.
     *
     * @param groupId Group id.
     * @param userId  User id.
     */
    public void joinChatgroup(String groupId, String userId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        JoinChatgroupRequest request = JoinChatgroupRequest.newBuilder()
                .setChatgroupId(groupId)
                .setUserId(userId)
                .build();

        CommonStatus commonStatus;
        try {
            commonStatus = stub.joinChatgroup(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != commonStatus.getRtn()) {
            throw new KeepRealBusinessException(commonStatus);
        }
    }

    /**
     * Retrieves chatgroup by id.
     *
     * @param id Chatgroup id.
     * @param userId User id.
     * @return {@link ChatgroupMessage}.
     */
    public ChatgroupMessage retrieveChatgroupById(String id, String userId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        RetrieveChatgroupByIdRequest request = RetrieveChatgroupByIdRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        ChatgroupResponse response;
        try {
            response = stub.retrieveChatgroupById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve chat group by id returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getChatgroup();
    }

    /**
     * Retrieves chatgroups by island id.
     *
     * @param islandId Island id.
     * @param userId   User id.
     * @param page     Page index.
     * @param pageSize Page size.
     * @return {@link IslandChatgroupsResponse}.
     */
    public IslandChatgroupsResponse retrieveChatgroupsByIslandId(String islandId, String userId, Integer page, Integer pageSize) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        RetrieveChatgroupsByIslandIdRequest request = RetrieveChatgroupsByIslandIdRequest.newBuilder()
                .setIslandId(islandId)
                .setUserId(userId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        IslandChatgroupsResponse response;
        try {
            response = stub.retrieveChatgroupsByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve chat groups returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Retrieves chatgroups by user id.
     *
     * @param userId User id.
     * @return {@link UserChatgroupsResponse}.
     */
    public UserChatgroupsResponse retrieveChatgroupsByUserId(String userId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        RetreiveChatgroupsByUserIdRequest request = RetreiveChatgroupsByUserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        UserChatgroupsResponse response;
        try {
            response = stub.retrieveChatgroupsByUserId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve chat groups returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }

    /**
     * Retrieves chatgroup member ids.
     *
     * @param groupId Group id.
     * @param userId  User id.
     * @return {@link ChatgroupMembersResponse}.
     */
    public ChatgroupMembersResponse retrieveChatgroupMembersByGroupId(String groupId, String userId, Integer page, Integer pageSize) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        RetrieveChatgroupMembersByGroupIdRequest request = RetrieveChatgroupMembersByGroupIdRequest.newBuilder()
                .setUserId(userId)
                .setGroupId(groupId)
                .setPageRequest(PaginationUtils.buildPageRequest(page, pageSize))
                .build();

        ChatgroupMembersResponse response;
        try {
            response = stub.retrieveChatgroupMembersById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Retrieve chat group members returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response;
    }


    /**
     * Checks if a string field is too long.
     *
     * @param str        String.
     * @param lowerLimit Limit.
     * @param upperLimit Limit.
     * @return Trimmed string.
     */
    private String checkLength(String str, int lowerLimit, int upperLimit) {
        String trimmed = str.trim();

        if (trimmed.length() > upperLimit || trimmed.length() < lowerLimit)
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);

        return trimmed;
    }

}