package com.keepreal.madagascar.asity.grpcController;

import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.EnableChatAccessRequest;
import com.keepreal.madagascar.asity.IslandChatAccessResponse;
import com.keepreal.madagascar.asity.RegisterRequest;
import com.keepreal.madagascar.asity.RegisterResponse;
import com.keepreal.madagascar.asity.RetrieveChatAccessByIslandIdAndUserIdRequest;
import com.keepreal.madagascar.asity.model.IslandChatAccess;
import com.keepreal.madagascar.asity.service.IslandChatAccessService;
import com.keepreal.madagascar.asity.service.RongCloudService;
import com.keepreal.madagascar.asity.util.CommonStatusUtils;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * Represents the chat grpc service.
 */
@GRpcService
public class ChatController extends ChatServiceGrpc.ChatServiceImplBase {

    private final RongCloudService rongCloudService;
    private final IslandChatAccessService islandChatAccessService;

    /**
     * Constructs the chat controller.
     *
     * @param rongCloudService        {@link RongCloudService}.
     * @param islandChatAccessService {@link IslandChatAccessService}.
     */
    public ChatController(RongCloudService rongCloudService,
                          IslandChatAccessService islandChatAccessService) {
        this.rongCloudService = rongCloudService;
        this.islandChatAccessService = islandChatAccessService;
    }

    /**
     * Registers a user.
     *
     * @param request          {@link RegisterRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void registerUser(RegisterRequest request,
                             StreamObserver<RegisterResponse> responseObserver) {
        RegisterResponse response;
        try {
            String token = this.rongCloudService.register(request.getUserId(), request.getUserName(), request.getPortraitUrl());
            response = RegisterResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                    .setToken(token)
                    .build();
        } catch (KeepRealBusinessException e) {
            response = RegisterResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(e.getErrorCode()))
                    .build();
        } catch (Exception e) {
            response = RegisterResponse.newBuilder()
                    .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_UNEXPECTED_ERROR))
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Enables the island chat access.
     *
     * @param request          {@link EnableChatAccessRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    public void enableChatAccess(EnableChatAccessRequest request,
                                 StreamObserver<CommonStatus> responseObserver) {
        this.islandChatAccessService.enable(request.getIslandId(), request.getUserId());

        CommonStatus response = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * Retrieves the chat access for an island and user.
     *
     * @param request            {@link RetrieveChatAccessByIslandIdAndUserIdRequest}.
     * @param responseObserver   {@link StreamObserver}.
     */
    public void retrieveChatAccessByIslandIdAndUserId(RetrieveChatAccessByIslandIdAndUserIdRequest request,
                                                      StreamObserver<IslandChatAccessResponse> responseObserver) {
        IslandChatAccess islandChatAccess = this.islandChatAccessService.createIslandChatAccess(request.getIslandId(), request.getUserId());

        // TODO: FILL THE RESPONSE
        IslandChatAccessResponse response = IslandChatAccessResponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
