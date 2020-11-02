package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.coua.MembershipServiceGrpc;
import com.keepreal.madagascar.coua.MembershipsResponse;
import com.keepreal.madagascar.coua.RetrieveMembershipsByUserIdRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembershipService {

    private final Channel channel;

    /**
     * Constructs the membership service.
     *
     * @param channel GRpc managed channel connection to service Coua.
     */
    public MembershipService(@Qualifier("couaChannel") Channel channel) {
        this.channel = channel;
    }

    public List<MembershipMessage> retrieveMembershipsByUserId(String userId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        RetrieveMembershipsByUserIdRequest request = RetrieveMembershipsByUserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        MembershipsResponse membershipsResponse;
        try {
            membershipsResponse = stub.retrieveMembershipsByUserId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != membershipsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(membershipsResponse.getStatus());
        }

        return membershipsResponse.getMessageList();
    }
}
