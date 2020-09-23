package com.keepreal.madagascar.fossa.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.vanga.RetrieveMembershipIdsRequest;
import com.keepreal.madagascar.vanga.RetrieveMembershipIdsResponse;
import com.keepreal.madagascar.vanga.SubscribeMembershipServiceGrpc;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represent the subscribe membership service.
 */
@Service
public class SubscribeMembershipService {

    private final Channel channel;

    public SubscribeMembershipService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * retrieve membership id list by user id and island id.
     *
     * @param userId    user id.
     * @param islandId  island id.
     * @return  membership id list.
     */
    public List<String> retrieveMembershipIds(String userId, String islandId) {
        SubscribeMembershipServiceGrpc.SubscribeMembershipServiceBlockingStub stub = SubscribeMembershipServiceGrpc.newBlockingStub(this.channel);

        RetrieveMembershipIdsRequest.Builder requestBuilder = RetrieveMembershipIdsRequest.newBuilder()
                .setUserId(userId);

        if (Objects.nonNull(islandId)) {
            requestBuilder.setIslandId(StringValue.of(islandId));
        }

        RetrieveMembershipIdsResponse response;
        try {
            response = stub.retrieveMembershipIdsByUserIdAndIslandId(requestBuilder.build());
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        return response.getMembershipIdsList();
    }
}
