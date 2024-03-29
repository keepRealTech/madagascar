package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.RetrieveMemberCountByIslandIdRequest;
import com.keepreal.madagascar.vanga.RetrieveMemberCountByMembershipIdRequest;
import com.keepreal.madagascar.vanga.RetrieveMemberCountResponse;
import com.keepreal.madagascar.vanga.RetrieveMembershipIdsRequest;
import com.keepreal.madagascar.vanga.RetrieveMembershipIdsResponse;
import com.keepreal.madagascar.vanga.RetrieveSubscribeMembershipRequest;
import com.keepreal.madagascar.vanga.RetrieveSubscribeMembershipResponse;
import com.keepreal.madagascar.vanga.SubscribeMembershipServiceGrpc;
import com.keepreal.madagascar.vanga.model.SubscribeMembership;
import com.keepreal.madagascar.vanga.service.SubscribeMembershipService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represent the subscribeMembership controller.
 */
@GRpcService
public class SubscribeMembershipGRpcController extends SubscribeMembershipServiceGrpc.SubscribeMembershipServiceImplBase {

    private final SubscribeMembershipService subscribeMembershipService;

    /**
     * Constructs the subscribeMembership service.
     *
     * @param subscribeMembershipService    {@link SubscribeMembershipService}.
     */
    public SubscribeMembershipGRpcController(SubscribeMembershipService subscribeMembershipService) {
        this.subscribeMembershipService = subscribeMembershipService;
    }

    @Override
    public void retrieveMemberCountByIslandId(RetrieveMemberCountByIslandIdRequest request, StreamObserver<RetrieveMemberCountResponse> responseObserver) {
        String islandId = request.getIslandId();
        Integer memberCount = subscribeMembershipService.getMemberCountByIslandId(islandId);

        responseObserver.onNext(RetrieveMemberCountResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setMemberCount(memberCount)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveMemberCountByMembershipId(RetrieveMemberCountByMembershipIdRequest request, StreamObserver<RetrieveMemberCountResponse> responseObserver) {
        String membershipId = request.getMembershipId();
        Integer memberCount = subscribeMembershipService.getMemberCountByMembershipId(membershipId);

        responseObserver.onNext(RetrieveMemberCountResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .setMemberCount(memberCount)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveMembershipIdsByUserIdAndIslandId(RetrieveMembershipIdsRequest request, StreamObserver<RetrieveMembershipIdsResponse> responseObserver) {
        List<String> membershipIdList;
        if (request.hasIslandId()) {
            membershipIdList = subscribeMembershipService.getMembershipIdListByUserIdAndIslandId(request.getUserId(), request.getIslandId().getValue());
        } else {
            membershipIdList = subscribeMembershipService.getMembershipIdListByUserId(request.getUserId());
        }

        responseObserver.onNext(RetrieveMembershipIdsResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllMembershipIds(membershipIdList)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveSubscribeMembershipByUserId(RetrieveSubscribeMembershipRequest request, StreamObserver<RetrieveSubscribeMembershipResponse> responseObserver) {
        String userId = request.getUserId();
        String islandId = request.getIslandId();

        List<SubscribeMembership> subscribeMemberships = this.subscribeMembershipService.retrieveSubscribeMembership(userId, islandId);

        responseObserver.onNext(RetrieveSubscribeMembershipResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllMessage(subscribeMemberships
                        .stream()
                        .map(this.subscribeMembershipService::getMessage)
                        .collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }
}
