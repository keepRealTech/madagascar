package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.RetrieveMemberCountByIslandIdRequest;
import com.keepreal.madagascar.vanga.RetrieveMemberCountByMembershipIdRequest;
import com.keepreal.madagascar.vanga.RetrieveMemberCountResponse;
import com.keepreal.madagascar.vanga.SubscribeMembershipServiceGrpc;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


/**
 * Represents the subscribe membership service.
 */
@Service
public class SubscribeMembershipService {

    private final Channel channel;

    /**
     * Constructor the subscribe membership service.
     *
     * @param channel   GRpc managed channel connection to service Vanga.
     */
    public SubscribeMembershipService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * retrieve the membership count by island id.
     *
     * @param islandId  island id.
     * @return  member count.
     */
    public Integer getMemberCountByIslandId(String islandId) {
        SubscribeMembershipServiceGrpc.SubscribeMembershipServiceBlockingStub stub = SubscribeMembershipServiceGrpc.newBlockingStub(this.channel);

        RetrieveMemberCountByIslandIdRequest request = RetrieveMemberCountByIslandIdRequest.newBuilder().setIslandId(islandId).build();

        RetrieveMemberCountResponse response;
        try {
            response = stub.retrieveMemberCountByIslandId(request);
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        return response.getMemberCount();
    }

    /**
     * retrieve the membership count by membership id.
     *
     * @param membershipId  membership id.
     * @return  member count.
     */
    public Integer getMemberCountByMembershipId(String membershipId) {
        SubscribeMembershipServiceGrpc.SubscribeMembershipServiceBlockingStub stub = SubscribeMembershipServiceGrpc.newBlockingStub(this.channel);

        RetrieveMemberCountByMembershipIdRequest request = RetrieveMemberCountByMembershipIdRequest.newBuilder().setMembershipId(membershipId).build();

        RetrieveMemberCountResponse response;
        try {
            response = stub.retrieveMemberCountByMembershipId(request);
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        return response.getMemberCount();
    }
}
