package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.DeleteChatgroupMembershipByMembershipIdRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Represents the chatgroup service.
 */
@Service
public class ChatgroupService {

    private final Channel channel;

    /**
     * Constructs the chatgroup service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public ChatgroupService(@Qualifier("asityChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Deletes the chatgroup membership.
     *
     * @param membershipId Membership id.
     */
    public void deleteChatgroupMembershipByMembershipId(String membershipId) {
        ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(this.channel);

        DeleteChatgroupMembershipByMembershipIdRequest request =
                DeleteChatgroupMembershipByMembershipIdRequest.newBuilder().setMemberhsipId(membershipId).build();

        try {
            stub.deleteChatgroupMembershipByMembershipId(request);
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }

}
