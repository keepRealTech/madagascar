package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CreateMembershipRequest;
import com.keepreal.madagascar.coua.FeedMembershipMessage;
import com.keepreal.madagascar.coua.FeedMembershipResponse;
import com.keepreal.madagascar.coua.MembershipIdRequest;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.coua.MembershipResponse;
import com.keepreal.madagascar.coua.MembershipServiceGrpc;
import com.keepreal.madagascar.coua.MembershipsResponse;
import com.keepreal.madagascar.coua.RetrieveMembershipsByIdsRequest;
import com.keepreal.madagascar.coua.RetrieveMembershipsByIslandIdsRequest;
import com.keepreal.madagascar.coua.RetrieveMembershipsRequest;
import com.keepreal.madagascar.coua.TopMembershipRequest;
import com.keepreal.madagascar.coua.UpdateMembershipRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

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

    public void topMembershipById(String membershipId, Boolean isRevoke, String userId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        TopMembershipRequest request = TopMembershipRequest.newBuilder()
                .setId(membershipId)
                .setIsRevoke(isRevoke)
                .setUserId(userId)
                .build();

        CommonStatus commonStatus;
        try {
            commonStatus = stub.topMembershipById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != commonStatus.getRtn()) {
            throw new KeepRealBusinessException(commonStatus);
        }
    }

    public void deactivateMembershipById(String membershipId, String userId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        MembershipIdRequest request = MembershipIdRequest.newBuilder()
                .setId(membershipId)
                .setUserId(userId)
                .build();

        CommonStatus commonStatus;
        try {
            commonStatus = stub.deactivateMembershipById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != commonStatus.getRtn()) {
            throw new KeepRealBusinessException(commonStatus);
        }
    }

    public void deleteMembershipById(String membershipId, String userId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        MembershipIdRequest request = MembershipIdRequest.newBuilder()
                .setId(membershipId)
                .setUserId(userId)
                .build();

        CommonStatus commonStatus;
        try {
            commonStatus = stub.deleteMembershipById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != commonStatus.getRtn()) {
            throw new KeepRealBusinessException(commonStatus);
        }
    }

    public MembershipMessage createMembership(String name, Integer pricePerMonth, List<String> descriptions, String islandId, String hostId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        String descriptionStr = String.join(",", descriptions);
        CreateMembershipRequest request = CreateMembershipRequest.newBuilder()
                .setName(name)
                .setPricePerMonth(pricePerMonth)
                .setIslandId(islandId)
                .setHostId(hostId)
                .setDescription(descriptionStr)
                .build();

        MembershipResponse membershipResponse;
        try {
            membershipResponse = stub.createMembership(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != membershipResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(membershipResponse.getStatus());
        }

        return membershipResponse.getMessage();
    }

    public MembershipMessage retrieveMembershipById(String membershipId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        MembershipIdRequest request = MembershipIdRequest.newBuilder().setId(membershipId).build();

        MembershipResponse membershipResponse;
        try {
            membershipResponse = stub.retrieveMembershipById(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != membershipResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(membershipResponse.getStatus());
        }

        return membershipResponse.getMessage();
    }

    public MembershipMessage updateMembershipById(String membershipId, String name, List<String> descriptions, Integer pricePerMonth, String userId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        UpdateMembershipRequest.Builder builder = UpdateMembershipRequest.newBuilder()
                .setId(membershipId)
                .setUserId(userId);
        if (!StringUtils.isEmpty(name)) {
            builder.setName(StringValue.of(name));
        }
        if (pricePerMonth != null) {
            builder.setPricePerMonth(Int32Value.of(pricePerMonth));
        }
        if (descriptions != null && descriptions.size() > 0) {
            String descriptionStr = descriptions.toString();
            builder.setDescription(StringValue.of(descriptionStr.substring(1, descriptionStr.length() - 1)));
        }

        MembershipResponse membershipResponse;
        try {
            membershipResponse = stub.updateMembership(builder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != membershipResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(membershipResponse.getStatus());
        }

        return membershipResponse.getMessage();
    }

    public List<MembershipMessage> retrieveMembershipsByIslandId(String islandId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        RetrieveMembershipsRequest request = RetrieveMembershipsRequest.newBuilder().setIslandId(islandId).build();

        MembershipsResponse membershipsResponse;
        try {
            membershipsResponse = stub.retrieveMembershipsByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != membershipsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(membershipsResponse.getStatus());
        }

        return membershipsResponse.getMessageList();
    }

    public List<FeedMembershipMessage> retrieveFeedMembershipsByIslandId(String islandId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        RetrieveMembershipsRequest request = RetrieveMembershipsRequest.newBuilder().setIslandId(islandId).build();

        FeedMembershipResponse feedMembershipResponse;
        try {
            feedMembershipResponse = stub.retrieveFeedMembershipsByIslandId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != feedMembershipResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(feedMembershipResponse.getStatus());
        }

        return feedMembershipResponse.getMessageList();
    }

    public List<MembershipMessage> retrieveMembershipsByIslandIds(Collection<String> islandIds) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        RetrieveMembershipsByIslandIdsRequest request = RetrieveMembershipsByIslandIdsRequest.newBuilder().addAllIslandIds(islandIds).build();

        MembershipsResponse membershipsResponse;
        try {
            membershipsResponse = stub.retrieveMembershipsByIslandIds(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != membershipsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(membershipsResponse.getStatus());
        }

        return membershipsResponse.getMessageList();
    }

    public List<MembershipMessage> retrieveMembershipsByIds(Iterable<String> ids) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        RetrieveMembershipsByIdsRequest request = RetrieveMembershipsByIdsRequest
                .newBuilder()
                .addAllIds(ids)
                .build();

        MembershipsResponse membershipsResponse;
        try {
            membershipsResponse = stub.retrieveMembershipsByIds(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != membershipsResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(membershipsResponse.getStatus());
        }

        return membershipsResponse.getMessageList();
    }

}
