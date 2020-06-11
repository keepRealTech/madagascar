package com.keepreal.madagascar.marty.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import com.keepreal.madagascar.coua.UserServiceGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Represents the user service.
 */
@Service
public class UserService {

    private final Channel channel;

    /**
     * Constructs the user service.
     *
     * @param channel Managed channel for grpc traffic.
     */
    public UserService(@Qualifier("couaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Retrieve device token by user id.
     *
     * @param userId    user id.
     * @return  {@link RetrieveDeviceTokenResponse}.
     */
    public RetrieveDeviceTokenResponse retrieveUserDeviceToken(String userId) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        RetrieveDeviceTokenRequest request = RetrieveDeviceTokenRequest.newBuilder().setUserId(userId).build();

        RetrieveDeviceTokenResponse response;
        try {
            response = stub.retrieveDeviceTokenByUserId(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        return response;
    }
}
