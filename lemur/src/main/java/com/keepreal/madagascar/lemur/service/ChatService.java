package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.ChatgroupMessage;
import com.keepreal.madagascar.asity.ChatgroupResponse;
import com.keepreal.madagascar.asity.CreateChatgroupRequest;
import com.keepreal.madagascar.asity.DismissChatgroupRequest;
import com.keepreal.madagascar.asity.EnableChatAccessRequest;
import com.keepreal.madagascar.asity.IslandChatAccessResponse;
import com.keepreal.madagascar.asity.RegisterRequest;
import com.keepreal.madagascar.asity.RegisterResponse;
import com.keepreal.madagascar.asity.RetrieveChatAccessByIslandIdAndUserIdRequest;
import com.keepreal.madagascar.asity.UpdateChatgroupRequest;
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

        if (Objects.nonNull(membershipIds)) {
            requestBuilder.addAllMembershipIds(membershipIds);
        }

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