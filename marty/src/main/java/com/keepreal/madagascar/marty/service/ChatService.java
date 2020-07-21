package com.keepreal.madagascar.marty.service;

import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.ChatgroupMembersResponse;
import com.keepreal.madagascar.asity.RetrieveChatgroupByIdRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChatService {

    private final Channel channel;

    public ChatService(@Qualifier("asityChannel") Channel channel) {
        this.channel = channel;
    }

    public List<String> retrieveChatgroupMemberIds(String chatGroupId, String userId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        ChatgroupMembersResponse response = null;
        try {
            response = stub.retrieveChatgroupMembersById(RetrieveChatgroupByIdRequest.newBuilder()
                    .setId(chatGroupId)
                    .setUserId(userId)
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMemberIdsList();
    }
}
