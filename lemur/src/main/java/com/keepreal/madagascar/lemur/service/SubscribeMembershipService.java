package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.vanga.RetrieveMembershipIdsRequest;
import com.keepreal.madagascar.vanga.RetrieveMembershipIdsResponse;
import com.keepreal.madagascar.vanga.SubscribeMembershipServiceGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.List;

/**
 * Represents the subscribe membership service.
 */
@Service
@Slf4j
public class SubscribeMembershipService {

    private final Channel channel;

    /**
     * Constructs the subscibe membership service.
     *
     * @param channel GRpc managed channel connection to service Vanga.
     */
    public SubscribeMembershipService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Retrieves subscribed membership ids by island and user.
     *
     * @param islandId Island id.
     * @param userId   User id.
     * @return Membership ids.
     */
    public List<String> retrieveSubscribedMembershipsByIslandIdAndUserId(String islandId, String userId) {
        SubscribeMembershipServiceGrpc.SubscribeMembershipServiceBlockingStub stub = SubscribeMembershipServiceGrpc.newBlockingStub(this.channel);

        RetrieveMembershipIdsRequest request = RetrieveMembershipIdsRequest.newBuilder()
                .setIslandId(islandId)
                .setUserId(userId)
                .build();

        RetrieveMembershipIdsResponse response;
        try {
            response = stub.retrieveMembershipIdsByUserIdAndIslandId(request);
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

        return response.getMembershipIdsList();
    }

}
