package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.DeviceType;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.GenderValue;
import com.keepreal.madagascar.common.IdentityType;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CheckUserMobileIsExistedRequest;
import com.keepreal.madagascar.coua.CheckUserMobileIsExistedResponse;
import com.keepreal.madagascar.coua.CreateOrUpdateUserPasswordRequest;
import com.keepreal.madagascar.coua.CreateOrUpdateUserQualificationsRequest;
import com.keepreal.madagascar.coua.CreateOrUpdateUserQualificationsResponse;
import com.keepreal.madagascar.coua.DeviceTokenRequest;
import com.keepreal.madagascar.coua.DeviceTokenResponse;
import com.keepreal.madagascar.coua.QualificationMessage;
import com.keepreal.madagascar.coua.QueryUserCondition;
import com.keepreal.madagascar.coua.RetreiveMultipleUsersByIdsRequest;
import com.keepreal.madagascar.coua.RetrieveSingleUserRequest;
import com.keepreal.madagascar.coua.RetrieveUserQualificationsRequest;
import com.keepreal.madagascar.coua.RetrieveUserQualificationsResponse;
import com.keepreal.madagascar.coua.SendOtpToMobileRequest;
import com.keepreal.madagascar.coua.SendOtpToMobileResponse;
import com.keepreal.madagascar.coua.UpdateUserByIdRequest;
import com.keepreal.madagascar.coua.UpdateUserMobileRequest;
import com.keepreal.madagascar.coua.UserResponse;
import com.keepreal.madagascar.coua.UserServiceGrpc;
import com.keepreal.madagascar.coua.UsersReponse;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import swagger.model.CommonResponse;
import swagger.model.PutUserMobileRequest;

import java.util.List;
import java.util.Objects;

/**
 * Represents the user service.
 */
@Service
@Slf4j
public class UserService {

    private static final int NAME_LENGTH_THRESHOLD = 32;
    private static final int DESCRIPTION_LENGTH_THRESHOLD = 120;

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
    @Cacheable(value = "UserMessage", key = "#id", cacheManager = "redisCacheManager")
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
    @CachePut(value = "UserMessage", key = "#id", cacheManager = "redisCacheManager")
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

        if (!Objects.isNull(description)) {
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

    /**
     * Retrieves all users by ids.
     *
     * @param userIds User ids.
     * @return {@link UserMessage}.
     */
    public List<UserMessage> retrieveUsersByIds(Iterable<String> userIds) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        RetreiveMultipleUsersByIdsRequest request = RetreiveMultipleUsersByIdsRequest.newBuilder()
                .addAllUserIds(userIds)
                .build();

        UsersReponse reponse;
        try {
            reponse = stub.retrieveUsersByIds(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(reponse)
                || !reponse.hasStatus()) {
            log.error(Objects.isNull(reponse) ? "Retrieve users returned null." : reponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != reponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(reponse.getStatus());
        }

        return reponse.getUsersList();
    }

    public void updateDeviceToken(String userId, String deviceToken, boolean isBind, Integer deviceType) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        DeviceTokenRequest request = DeviceTokenRequest.newBuilder()
                .setUserId(userId)
                .setDeviceToken(deviceToken)
                .setIsBind(isBind)
                .setDeviceType(DeviceType.forNumber(deviceType))
                .build();

        DeviceTokenResponse deviceTokenResponse;
        try {
            deviceTokenResponse = stub.updateDeviceToken(request);
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

    /**
     * 向指定手机号发送验证码
     *
     * @param mobile 手机号
     */
    public void sendOtpToMobile(String code, String mobile) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        SendOtpToMobileRequest request = SendOtpToMobileRequest.newBuilder()
                .setMobile(mobile)
                .setCode(code)
                .build();

        SendOtpToMobileResponse response;
        try {
            response = stub.sendOtpToMobile(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "send otp to mobile return null" : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

    }

    /**
     * 更新当前用户手机号
     *
     * @param userId    User id.
     * @param mobile    Mobile.
     * @param otp       One time password.
     * @return {@link UserMessage}
     */
    @CachePut(value = "UserMessage", key = "#userId", cacheManager = "redisCacheManager")
    public UserMessage updateUserMobilePhone(String userId, String code, String mobile, Integer otp) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        UpdateUserMobileRequest request = UpdateUserMobileRequest.newBuilder()
                .setUserId(userId)
                .setCode(code)
                .setMobile(mobile)
                .setOtp(otp)
                .build();

        UserResponse response;
        try {
            response = stub.updateUserMobile(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "update user mobile return null" : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getUser();
    }

    /**
     * 判断 手机号是否已被绑定
     *
     * @param mobile 手机号
     */
    public void checkUserMobileIsExisted(String userId, String code, String mobile) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);
        CheckUserMobileIsExistedRequest request = CheckUserMobileIsExistedRequest.newBuilder()
                .setUserId(userId)
                .setCode(code)
                .setMobile(mobile)
                .build();
        CheckUserMobileIsExistedResponse response;

        try {
            response = stub.checkUserMobileIsExisted(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "check user mobile return null" : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }

    public List<QualificationMessage> retrieveUserQualifications(String userId) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        RetrieveUserQualificationsResponse response;

        try {
            response = stub.retrieveUserQualifications(RetrieveUserQualificationsRequest.newBuilder().setUserId(userId).build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "retrieve user qualifications returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMessageList();
    }

    public List<QualificationMessage> createOrUpdateUserQualifications(String userId, List<QualificationMessage> qualificationMessages) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);

        CreateOrUpdateUserQualificationsResponse response;
        try {
            response = stub.createOrUpdateUserQualifications(CreateOrUpdateUserQualificationsRequest.newBuilder()
                    .setUserId(userId)
                    .addAllMessage(qualificationMessages)
                    .build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "create or update user qualifications returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getMessageList();
    }

    /**
     * 创建/更新 用户密码
     *
     * @param userId 用户id
     * @param code   区号
     * @param mobile 手机
     * @param otp    验证码
     * @param password 密码
     */
    @CachePut(value = "UserMessage", key = "#userId", cacheManager = "redisCacheManager")
    public UserMessage createOrUpdateUserPassword(String userId, String code, String mobile, Integer otp, String password) {
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(this.channel);
        CreateOrUpdateUserPasswordRequest request = CreateOrUpdateUserPasswordRequest.newBuilder()
                .setUserId(userId)
                .setCode(code)
                .setMobile(mobile)
                .setOtp(otp)
                .setPassword(password)
                .build();

        UserResponse response;
        try {
            response = stub.createOrUpdateUserPassword(request);
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "create or update user password returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }

        return response.getUser();
    }

    /**
     * Trims and checks the string length validity.
     *
     * @param str       String.
     * @param threshold Max length.
     * @return Trimmed string.
     */
    private String checkLength(String str, int threshold) {
        String trimmed = str.trim();

        if (trimmed.length() > threshold)
            throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
        return trimmed;
    }

}
