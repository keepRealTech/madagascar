package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.GenderValue;
import com.keepreal.madagascar.common.IdentityType;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.QueryUserCondition;
import com.keepreal.madagascar.coua.RetrieveSingleUserRequest;
import com.keepreal.madagascar.coua.UpdateUserByIdRequest;
import com.keepreal.madagascar.coua.UserResponse;
import com.keepreal.madagascar.coua.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Represents the user service.
 */
@Service
@Slf4j
public class UserService {

    private final ManagedChannel managedChannel;

    /**
     * Constructs the user service.
     *
     * @param managedChannel GRpc managed channel connection to service Coua.
     */
    public UserService(@Qualifier("couaChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * Retrieves an user by id.
     *
     * @param id User id.
     * @return {@link UserMessage}.
     */
    @Cacheable(value = "user", key = "#id")
    public UserMessage retrieveUserById(String id) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.managedChannel);

        QueryUserCondition condition = QueryUserCondition.newBuilder()
                .setId(StringValue.of(id))
                .build();

        RetrieveSingleUserRequest request = RetrieveSingleUserRequest.newBuilder()
                .setCondition(condition)
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

    /**
     * Updates an user by id.
     *
     * @param id               Id.
     * @param name             Name.
     * @param portraitImageUri Portrait image uri.
     * @param gender           Gender.
     * @param description      Description.
     * @param city             City.
     * @param birthday         Birthday.
     * @param identityTypes    Identity types.
     * @return {@link UserResponse}.
     */
    @CacheEvict(value = "user", key = "#id")
    public UserMessage updateUser(String id,
                                  String name,
                                  String portraitImageUri,
                                  Gender gender,
                                  String description,
                                  String city,
                                  String birthday,
                                  List<IdentityType> identityTypes) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.managedChannel);

        UpdateUserByIdRequest.Builder requestBuilder = UpdateUserByIdRequest.newBuilder()
                .setId(id);

        if (!StringUtils.isEmpty(name)) {
            requestBuilder.setName(StringValue.of(name));
        }

        if (!StringUtils.isEmpty(portraitImageUri)) {
            requestBuilder.setPortraitImageUri(StringValue.of(portraitImageUri));
        }

        if (!StringUtils.isEmpty(description)) {
            requestBuilder.setDescription(StringValue.of(description));
        }

        if (!StringUtils.isEmpty(city)) {
            requestBuilder.setCity(StringValue.of(city));
        }

        if (!StringUtils.isEmpty(birthday)) {
            requestBuilder.setBirthday(StringValue.of(birthday));
        }

        if (Objects.nonNull(gender)) {
            requestBuilder.setGender(GenderValue.newBuilder().setValue(gender));
        }

        if (Objects.nonNull(identityTypes)) {
            requestBuilder.addAllIdentities(identityTypes);
        }

        UserResponse userResponse;
        try {
            userResponse = stub.updateUserById(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(userResponse)
                || !userResponse.hasStatus()) {
            log.error(Objects.isNull(userResponse) ? "Update user returned null." : userResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != userResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(userResponse.getStatus());
        }

        return userResponse.getUser();
    }

}
