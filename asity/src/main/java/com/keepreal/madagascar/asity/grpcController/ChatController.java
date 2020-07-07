package com.keepreal.madagascar.asity.grpcController;

import com.keepreal.madagascar.asity.ChatServiceGrpc;
import com.keepreal.madagascar.asity.RegisterRequest;
import com.keepreal.madagascar.asity.RegisterResponse;
import com.keepreal.madagascar.asity.service.RongCloudService;
import com.keepreal.madagascar.asity.util.CommonStatusUtils;
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

    /**
     * Constructs the chat controller.
     *
     * @param rongCloudService {@link RongCloudService}.
     */
    public ChatController(RongCloudService rongCloudService) {
        this.rongCloudService = rongCloudService;
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

}
