package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.GenderValue;
import com.keepreal.madagascar.common.IdentityType;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.DeviceTokenRequest;
import com.keepreal.madagascar.coua.DeviceTokenResponse;
import com.keepreal.madagascar.coua.QueryUserCondition;
import com.keepreal.madagascar.coua.RetrieveSingleUserRequest;
import com.keepreal.madagascar.coua.UpdateUserByIdRequest;
import com.keepreal.madagascar.coua.UserResponse;
import com.keepreal.madagascar.coua.UserServiceGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private static final int NAME_LENGTH_THRESHOLD = 32;
    private static final int DESCRIPTION_LENGTH_THRESHOLD = 60;

    private final Channel channel;

    /**
     * Constructs the user service.
     *
     * @param channel GRpc managed channel connection to service Coua.
     */
    public UserService(@Qualifier("couaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Retrieves an user by id.
     *
     * @param id User id.
     * @return {@link UserMessage}.
     */
    public UserMessage retrieveUserById(String id) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

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
    public UserMessage updateUser(String id,
                                  String name,
                                  String portraitImageUri,
                                  Gender gender,
                                  String description,
                                  String city,
                                  String birthday,
                                  List<IdentityType> identityTypes) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        UpdateUserByIdRequest.Builder requestBuilder = UpdateUserByIdRequest.newBuilder()
                .setId(id);

        if (!StringUtils.isEmpty(name)) {
            name = checkLength(name, NAME_LENGTH_THRESHOLD);
            requestBuilder.setName(StringValue.of(name));
        }

        if (!StringUtils.isEmpty(portraitImageUri)) {
            requestBuilder.setPortraitImageUri(StringValue.of(portraitImageUri));
        }

        if (!StringUtils.isEmpty(description)) {
            description = checkLength(description, DESCRIPTION_LENGTH_THRESHOLD);
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

    public void setDeviceToken(String userId, String deviceToken, boolean isBind) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        DeviceTokenRequest request = DeviceTokenRequest.newBuilder()
                .setUserId(userId)
                .setDeviceToken(deviceToken)
                .setIsBind(isBind)
                .build();

        DeviceTokenResponse deviceTokenResponse;
        try {
            deviceTokenResponse = stub.setDeviceToken(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(deviceTokenResponse)
                || !deviceTokenResponse.hasStatus()) {
            log.error(Objects.isNull(deviceTokenResponse) ? "set device token returned null." : deviceTokenResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != deviceTokenResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(deviceTokenResponse.getStatus());
        }
    }

    private String checkLength(String str, int threshold) {
        String trimmed = str.trim();

        if (trimmed.length() > threshold)
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        return trimmed;
    }

}
