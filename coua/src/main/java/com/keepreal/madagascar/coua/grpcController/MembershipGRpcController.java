package com.keepreal.madagascar.coua.grpcController;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
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
import com.keepreal.madagascar.coua.model.IslandInfo;
import com.keepreal.madagascar.coua.model.MembershipInfo;
import com.keepreal.madagascar.coua.service.IslandInfoService;
import com.keepreal.madagascar.coua.service.MembershipService;
import com.keepreal.madagascar.coua.service.SkuService;
import com.keepreal.madagascar.coua.service.SubscribeMembershipService;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  Represents the membership grpc controller.
 */
@GRpcService
@Slf4j
public class MembershipGRpcController extends MembershipServiceGrpc.MembershipServiceImplBase {

    private final MembershipService membershipService;
    private final SubscribeMembershipService subscribeMembershipService;
    private final SkuService skuService;
    private final IslandInfoService islandInfoService;

    /**
     * Constructor the membership grpc controller.
     *
     * @param membershipService             {@link MembershipService}.
     * @param subscribeMembershipService    {@link SubscribeMembershipService}.
     * @param skuService                    {@link SkuService}.
     * @param islandInfoService             {@link IslandInfoService}.
     */
    public MembershipGRpcController(MembershipService membershipService,
                                    SubscribeMembershipService subscribeMembershipService,
                                    SkuService skuService,
                                    IslandInfoService islandInfoService) {
        this.membershipService = membershipService;
        this.subscribeMembershipService = subscribeMembershipService;
        this.skuService = skuService;
        this.islandInfoService = islandInfoService;
    }

    /**
     * Implements the top membership method.
     *
     * @param request           {@link TopMembershipRequest}.
     * @param responseObserver  {@link CommonStatus}.
     */
    @Override
    public void topMembershipById(TopMembershipRequest request, StreamObserver<CommonStatus> responseObserver) {
        String membershipId = request.getId();
        boolean isRevoke = request.getIsRevoke();
        MembershipInfo membership = membershipService.getMembershipById(membershipId);
        if (membership == null) {
            responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_MEMBERSHIP_NOT_FOUNT_ERROR));
            responseObserver.onCompleted();
            return;
        }
        if (isRevoke) {
            membership.setTop(false);
            updateAndResponse(membership, responseObserver);
        } else {
            membershipService.revokeTopMembership(membership.getIslandId());
            membership.setTop(true);
            updateAndResponse(membership, responseObserver);
        }
    }

    /**
     * Implements the retrieve membership by id method.
     *
     * @param request           {@link MembershipIdRequest}.
     * @param responseObserver  {@link MembershipResponse}.
     */
    @Override
    public void retrieveMembershipById(MembershipIdRequest request, StreamObserver<MembershipResponse> responseObserver) {
        String membershipId = request.getId();
        MembershipInfo membership = membershipService.getMembershipById(membershipId);
        if (membership == null) {
            responseObserver.onNext(MembershipResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_MEMBERSHIP_NOT_FOUNT_ERROR))
                    .build());
            responseObserver.onCompleted();
            return;
        }
        responseObserver.onNext(MembershipResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setMessage(membershipService.getMembershipMessage(membership))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * Implements the deactivate membership by id method.
     *
     * @param request           {@link MembershipIdRequest}.
     * @param responseObserver  {@link CommonStatus}.
     */
    @Override
    public void deactivateMembershipById(MembershipIdRequest request, StreamObserver<CommonStatus> responseObserver) {
        String membershipId = request.getId();
        MembershipInfo membership = membershipService.getMembershipById(membershipId);
        if (membership == null) {
            responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_MEMBERSHIP_NOT_FOUNT_ERROR));
            responseObserver.onCompleted();
            return;
        }

        this.membershipService.deactivateMembership(membership);

        responseObserver.onNext(CommonStatusUtils.getSuccStatus());
        responseObserver.onCompleted();
    }

    /**
     * Implements the delete membership by id method.
     *
     * @param request           {@link MembershipIdRequest}.
     * @param responseObserver  {@link CommonStatus}.
     */
    @Override
    public void deleteMembershipById(MembershipIdRequest request, StreamObserver<CommonStatus> responseObserver) {
        String membershipId = request.getId();
        MembershipInfo membership = membershipService.getMembershipById(membershipId);
        if (membership == null) {
            responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_MEMBERSHIP_NOT_FOUNT_ERROR));
            responseObserver.onCompleted();
            return;
        }
        if (subscribeMembershipService.getMemberCountByMembershipId(membershipId) > 0) {
            responseObserver.onNext(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_MEMBERSHIP_NOT_FOUNT_ERROR));
            responseObserver.onCompleted();
            return;
        }

        this.membershipService.deleteMembership(membership);
        responseObserver.onNext(CommonStatusUtils.getSuccStatus());
        responseObserver.onCompleted();
    }

    /**
     * Implements the update membership method.
     *
     * @param request           {@link UpdateMembershipRequest}.
     * @param responseObserver  {@link MembershipResponse}.
     */
    @Override
    public void updateMembership(UpdateMembershipRequest request, StreamObserver<MembershipResponse> responseObserver) {
        MembershipInfo membership = membershipService.getMembershipById(request.getId());

        if (membership == null) {
            responseObserver.onNext(MembershipResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_MEMBERSHIP_NOT_FOUNT_ERROR))
                    .build());
            responseObserver.onCompleted();
            return;
        }

        if (!request.hasName() && !request.hasPricePerMonth() && request.hasDescription()) {
            membership.setDescription(request.getDescription().getValue());
            membership = this.membershipService.updateMembership(membership);
        } else {
            membership = this.membershipService.updateMembershipWithSku(membership, request);
        }

        responseObserver.onNext(MembershipResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setMessage(membershipService.getMembershipMessage(membership))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * Implements the create membership method.
     *
     * @param request           {@link CreateMembershipRequest}.
     * @param responseObserver  {@link MembershipResponse}.
     */
    @Override
    public void createMembership(CreateMembershipRequest request, StreamObserver<MembershipResponse> responseObserver) {
        String islandId = request.getIslandId();
        IslandInfo islandInfo = islandInfoService.findTopByIdAndDeletedIsFalse(islandId);
        if (islandInfo == null || !islandInfo.getHostId().equals(request.getHostId())) {
            responseObserver.onNext(MembershipResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_INVALID_ARGUMENT))
                    .build());
            responseObserver.onCompleted();
            log.error("user id is not island host id! userId is {}, islandId is {}", request.getHostId(), islandId);
            return;
        }
        MembershipInfo membershipInfo = new MembershipInfo();
        membershipInfo.setName(request.getName());
        membershipInfo.setIslandId(islandId);
        membershipInfo.setHostId(request.getHostId());
        membershipInfo.setDescription(request.getDescription());
        membershipInfo.setPricePerMonth(request.getPricePerMonth());
        MembershipInfo membership = membershipService.createMembership(membershipInfo);

        responseObserver.onNext(MembershipResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setMessage(membershipService.getMembershipMessage(membership))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveMembershipsByIslandId(RetrieveMembershipsRequest request, StreamObserver<MembershipsResponse> responseObserver) {
        String islandId = request.getIslandId();
        List<MembershipMessage> membershipMessages = membershipService.getMembershipListByIslandId(islandId)
                .stream()
                .map(membershipService::getMembershipMessage)
                .collect(Collectors.toList());

        responseObserver.onNext(MembershipsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllMessage(membershipMessages)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveFeedMembershipsByIslandId(RetrieveMembershipsRequest request, StreamObserver<FeedMembershipResponse> responseObserver) {
        String islandId = request.getIslandId();
        List<FeedMembershipMessage> feedMembershipMessages = new ArrayList<>();
        feedMembershipMessages.addAll(membershipService.generateBaseMessage(islandId));
        feedMembershipMessages.addAll(membershipService.getMembershipListByIslandId(islandId)
                .stream()
                .map(membershipService::getFeedMembershipMessage)
                .collect(Collectors.toList()));

        responseObserver.onNext(FeedMembershipResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllMessage(feedMembershipMessages)
                .build());
        responseObserver.onCompleted();
    }

    private void updateAndResponse(MembershipInfo membershipInfo, StreamObserver<CommonStatus> responseObserver) {
        membershipService.updateMembership(membershipInfo);
        responseObserver.onNext(CommonStatusUtils.getSuccStatus());
        responseObserver.onCompleted();
    }
}
