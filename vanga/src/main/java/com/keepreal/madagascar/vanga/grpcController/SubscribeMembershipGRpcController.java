package com.keepreal.madagascar.vanga.grpcController;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.vanga.RetrieveMemberCountByIslandIdRequest;
import com.keepreal.madagascar.vanga.RetrieveMemberCountByMembershipIdRequest;
import com.keepreal.madagascar.vanga.RetrieveMemberCountResponse;
import com.keepreal.madagascar.vanga.SubscribeMembershipServiceGrpc;
import com.keepreal.madagascar.vanga.service.SubscribeMembershipService;
import com.keepreal.madagascar.vanga.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

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
}
