package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.QueryUserCondition;
import com.keepreal.madagascar.coua.RetrieveSingleUserRequest;
import com.keepreal.madagascar.coua.UserResponse;
import com.keepreal.madagascar.coua.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Represents the user service.
 */
@Service
@Slf4j
public class UserService {

    private final ManagedChannel managedChannel;

    /**
     * Constructs the user service.
     * @param managedChannel GRpc managed channel connection to service Coua.
     */
    public UserService(@Qualifier("couaChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * Retrieves an user by id.
     * @param id User id.
     * @return {@link UserResponse}.
     */
    public UserResponse retrieveUserById(String id) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.managedChannel);

        QueryUserCondition condition = QueryUserCondition.newBuilder()
                .setId(StringValue.of(id))
                .build();

        RetrieveSingleUserRequest request = RetrieveSingleUserRequest.newBuilder()
                .setCondition(condition)
                .build();

        try {
            return stub.retrieveSingleUser(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(exception);
        }
    }

}
