package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.MembershipIdRequest;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.coua.MembershipResponse;
import com.keepreal.madagascar.coua.MembershipServiceGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Represents the membership service.
 */
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

    /**
     * Retrieves a membership by id.
     *
     * @param membershipId Membership id.
     * @return {@link MembershipMessage}.
     */
    public MembershipMessage retrieveMembershipById(String membershipId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        MembershipIdRequest request = MembershipIdRequest.newBuilder().setId(membershipId).build();

        MembershipResponse membershipResponse;
        try {
            membershipResponse = stub.retrieveMembershipById(request);
        } catch (StatusRuntimeException exception) {
            return null;
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != membershipResponse.getStatus().getRtn()) {
            return null;
        }

        return membershipResponse.getMessage();
    }

}
