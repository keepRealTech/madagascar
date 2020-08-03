package com.keepreal.madagascar.coua.grpcController;

import com.google.protobuf.Empty;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.IslandIdentitiesResponse;
import com.keepreal.madagascar.coua.IslandIdentityServiceGrpc;
import com.keepreal.madagascar.coua.model.IslandIdentity;
import com.keepreal.madagascar.coua.service.IslandIdentityService;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the island identity grpc controller.
 */
@GRpcService
public class IslandIdentityGRpcController extends IslandIdentityServiceGrpc.IslandIdentityServiceImplBase {

    private final IslandIdentityService islandIdentityService;

    /**
     * Constructs the island identity grpc controller.
     *
     * @param islandIdentityService {@link IslandIdentityService}.
     */
    public IslandIdentityGRpcController(IslandIdentityService islandIdentityService) {
        this.islandIdentityService = islandIdentityService;
    }

    /**
     * Retrieves the active island identities.
     *
     * @param request          {@link Empty}.
     * @param responseObserver {@link IslandIdentitiesResponse}.
     */
    @Override
    public void retrieveActiveIslandIdentities(Empty request,
                                               StreamObserver<IslandIdentitiesResponse> responseObserver) {
        List<IslandIdentity> islandIdentities = this.islandIdentityService.retrieveActiveIslandIdentities();

        IslandIdentitiesResponse response = IslandIdentitiesResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllIslandIdentities(islandIdentities.stream()
                        .map(this.islandIdentityService::getIslandIdentityMessage)
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
