package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.RegisterRequest;
import com.keepreal.madagascar.asity.RegisterResponse;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Represents the chat service.
 */
@Service
@Slf4j
public class ChatService {

    private Channel channel;

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

}