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
import com.keepreal.madagascar.coua.RetrieveMembershipsRequest;
import com.keepreal.madagascar.coua.TopMembershipRequest;
import com.keepreal.madagascar.coua.UpdateMembershipRequest;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 *  Represents the membership service.
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

    public void topMembershipById(String membershipId, Boolean isRevoke) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        TopMembershipRequest request = TopMembershipRequest.newBuilder()
                .setId(membershipId)
                .setIsRevoke(isRevoke)
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

    public void deactivateMembershipById(String membershipId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        MembershipIdRequest request = MembershipIdRequest.newBuilder().setId(membershipId).build();

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

    public void deleteMembershipById(String membershipId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        MembershipIdRequest request = MembershipIdRequest.newBuilder().setId(membershipId).build();

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

    public MembershipMessage createMembership(String name, Integer pricePreMonth, List<String> descriptions, String islandId, String hostId) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        String descriptionStr = descriptions.toString();
        CreateMembershipRequest request = CreateMembershipRequest.newBuilder()
                .setName(name)
                .setPricePerMonth(pricePreMonth)
                .setIslandId(islandId)
                .setHostId(hostId)
                .setDescription(descriptionStr.substring(1, descriptionStr.length() - 1))
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

    public MembershipMessage updateMembershipById(String membershipId, String name, List<String> descriptions, Integer pricePreMonth) {
        MembershipServiceGrpc.MembershipServiceBlockingStub stub = MembershipServiceGrpc.newBlockingStub(this.channel);

        UpdateMembershipRequest.Builder builder = UpdateMembershipRequest.newBuilder().setId(membershipId);
        if (!StringUtils.isEmpty(name)) {
            builder.setName(StringValue.of(name));
        }
        if (pricePreMonth != null) {
            builder.setPricePreMonth(Int32Value.of(pricePreMonth));
        }
        if (descriptions.size() > 0) {
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

    public List<MembershipMessage> RetrieveMembershipsByIslandId(String islandId) {
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

    public List<FeedMembershipMessage> RetrieveFeedMembershipsByIslandId(String islandId) {
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
}
