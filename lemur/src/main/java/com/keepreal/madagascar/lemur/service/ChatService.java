package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.ChatgroupMessage;
import com.keepreal.madagascar.asity.ChatgroupResponse;
import com.keepreal.madagascar.asity.CreateChatgroupRequest;
import com.keepreal.madagascar.asity.EnableChatAccessRequest;
import com.keepreal.madagascar.asity.IslandChatAccessResponse;
import com.keepreal.madagascar.asity.RegisterRequest;
import com.keepreal.madagascar.asity.RegisterResponse;
import com.keepreal.madagascar.asity.RetrieveChatAccessByIslandIdAndUserIdRequest;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Represents the chat service.
 */
@Service
@Slf4j
public class ChatService {

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
        assert !StringUtils.isEmpty(user.getId());

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
     * @param userId   User id.
     */
    public void enableChatAccess(String islandId, String userId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        EnableChatAccessRequest request = EnableChatAccessRequest.newBuilder()
                .setIslandId(islandId)
                .setUserId(userId)
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
     * @param userId   User id.
     * @return {@link IslandChatAccessResponse}.
     */
    public IslandChatAccessResponse retrieveChatAccessByIslandIdAndUserId(String islandId, String userId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        RetrieveChatAccessByIslandIdAndUserIdRequest request = RetrieveChatAccessByIslandIdAndUserIdRequest.newBuilder()
                .setIslandId(islandId)
                .setUserId(userId)
                .build();

        IslandChatAccessResponse response;
        try {
            response = stub.retrieveChatAccessByIslandIdAndUserId(request);
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
     * @param islandId          Island id.
     * @param userId            User id.
     * @param name              Group name.
     * @param membershipIds     Membership ids.
     * @param bulletin          Bulletin.
     * @return {@link ChatgroupMessage}.
     */
    public ChatgroupMessage createChatgroup(String islandId, String userId, String name, List<String> membershipIds, String bulletin) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        CreateChatgroupRequest request = CreateChatgroupRequest.newBuilder()
                .setBulletin(StringUtils.isEmpty(bulletin) ? "" : bulletin)
                .setIslandId(islandId)
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

}