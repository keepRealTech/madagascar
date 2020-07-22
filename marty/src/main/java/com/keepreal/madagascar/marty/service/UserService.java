package com.keepreal.madagascar.marty.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.QueryUserCondition;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensByUserIdListRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensByUserIdListResponse;
import com.keepreal.madagascar.coua.RetrieveSingleUserRequest;
import com.keepreal.madagascar.coua.UserResponse;
import com.keepreal.madagascar.coua.UserServiceGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Represents the user service.
 */
@Service
@Slf4j
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

    public RetrieveDeviceTokensByUserIdListResponse retrieveDeviceTokensByUserIdList(List<String> userIdList) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        RetrieveDeviceTokensByUserIdListRequest request = RetrieveDeviceTokensByUserIdListRequest.newBuilder()
                .addAllUserIds(userIdList)
                .build();

        RetrieveDeviceTokensByUserIdListResponse response;
        try {
            response = stub.retrieveDeviceTokensByUserIdList(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        return response;
    }

    public UserMessage retrieveUserInfoById(String userId) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        RetrieveSingleUserRequest request = RetrieveSingleUserRequest.newBuilder()
                .setCondition(QueryUserCondition.newBuilder()
                        .setId(StringValue.of(userId))
                        .build())
                .build();

        UserResponse userResponse;
        try {
            userResponse = stub.retrieveSingleUser(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(userResponse)
                || !userResponse.hasStatus()) {
            log.error(Objects.isNull(userResponse) ? "Retrieve user returned null." : userResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != userResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(userResponse.getStatus());
        }

        return userResponse.getUser();
    }
}
