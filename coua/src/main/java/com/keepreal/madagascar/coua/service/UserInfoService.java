package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.IdentityType;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.NewUserRequest;
import com.keepreal.madagascar.coua.QueryUserCondition;
import com.keepreal.madagascar.coua.RetrieveSingleUserRequest;
import com.keepreal.madagascar.coua.UpdateUserByIdRequest;
import com.keepreal.madagascar.coua.UserResponse;
import com.keepreal.madagascar.coua.UserServiceGrpc;
import com.keepreal.madagascar.coua.dao.UserInfoRepository;
import com.keepreal.madagascar.coua.model.UserInfo;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@GRpcService
public class UserInfoService extends UserServiceGrpc.UserServiceImplBase {

    private final UserInfoRepository userInfoRepository;
    private final UserIdentityService userIdentityService;

    @Autowired
    public UserInfoService(UserInfoRepository userInfoRepository, UserIdentityService userIdentityService) {
        this.userInfoRepository = userInfoRepository;
        this.userIdentityService = userIdentityService;
    }

    @Override
    public void createUser(NewUserRequest request, StreamObserver<UserResponse> responseObserver) {
        Long userId = 0L;//todo: 从id生成器中获取
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setNickName(request.getName().getValue());
        userInfo.setPortraitImageUri(request.getPortraitImageUri().getValue());
        userInfo.setGender(request.getGender().getValueValue());
        userInfo.setCity(request.getCity().getValue());
        userInfo.setDescription(request.getDescription().getValue());
        userInfo.setUnionId(request.getUnionId());

        String birthdayStr = request.getBirthday().getValue();
        if (!StringUtils.isEmpty(birthdayStr)) {
            userInfo.setBirthday(Date.valueOf(birthdayStr));
        }
        if (request.getIdentitiesCount() > 0) {
            userIdentityService.saveUserIdentities(request.getIdentitiesValueList(), userId);
        }
        userInfo.setCreatedTime(System.currentTimeMillis());
        userInfo.setUpdatedTime(System.currentTimeMillis());

        saveAndResponse(userInfo, responseObserver);
    }

    @Override
    public void retrieveSingleUser(RetrieveSingleUserRequest request, StreamObserver<UserResponse> responseObserver) {
        QueryUserCondition queryUserCondition = request.getCondition();
        UserInfo userInfo = null;
        UserResponse.Builder responseBuilder = UserResponse.newBuilder();
        if (queryUserCondition.hasId()) {
            userInfo = userInfoRepository.findUserInfoByIdAndDeletedIsFalse(Long.valueOf(queryUserCondition.getId().getValue()));
        }
        if (queryUserCondition.hasUnionId()) {
            userInfo = userInfoRepository.findUserInfoByUnionIdAndDeletedIsFalse(queryUserCondition.getUnionId().getValue());
        }
        if (userInfo == null) {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
        } else {
            UserMessage userMessage = getUserMessage(userInfo);
            responseBuilder.setUser(userMessage);
        }
        UserResponse userResponse = responseBuilder.build();
        responseObserver.onNext(userResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateUserById(UpdateUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        Long userId = Long.valueOf(request.getId());
        UserInfo userInfo = userInfoRepository.findUserInfoByIdAndDeletedIsFalse(userId);
        if (request.hasName()) {
            userInfo.setNickName(request.getName().getValue());
        }
        if (request.hasPortraitImageUri()) {
            userInfo.setPortraitImageUri(request.getPortraitImageUri().getValue());
        }
        if (request.hasGender()) {
            userInfo.setGender(request.getGender().getValueValue());
        }
        if (request.hasCity()) {
            userInfo.setCity(request.getCity().getValue());
        }
        if (request.hasDescription()) {
            userInfo.setDescription(request.getDescription().getValue());
        }
        if (request.hasBirthday()) {
            String birthdayStr = request.getBirthday().getValue();
            if (!StringUtils.isEmpty(birthdayStr)) {
                userInfo.setBirthday(Date.valueOf(birthdayStr));
            }
        }
        if (request.getIdentitiesCount() > 0) {
            userIdentityService.saveUserIdentities(request.getIdentitiesValueList(), userId);
        }
        saveAndResponse(userInfo, responseObserver);
    }

    public UserInfo getUserInfoById(Long userId) {
        return userInfoRepository.findUserInfoByIdAndDeletedIsFalse(userId);
    }

    public UserMessage getUserMessageById(Long userId) {
        UserInfo userInfo = userInfoRepository.findUserInfoByIdAndDeletedIsFalse(userId);
        if (userInfo == null) {
            return null;
        }
        return getUserMessage(userInfo);
    }

    public List<UserMessage> getUserMessageListByIdList(List<Long> userIdList) {
        List<UserInfo> userInfoList = userInfoRepository.findAllById(userIdList);
        return userInfoList.stream().map(this::getUserMessage).collect(Collectors.toList());
    }

    private void saveAndResponse(UserInfo userInfo, StreamObserver<UserResponse> responseObserver) {
        UserInfo save = userInfoRepository.save(userInfo);

        UserMessage userMessage = getUserMessage(save);
        UserResponse userResponse = UserResponse.newBuilder().setUser(userMessage).build();
        responseObserver.onNext(userResponse);
        responseObserver.onCompleted();
    }

    private UserMessage getUserMessage(UserInfo userInfo) {
        List<Integer> identities = userIdentityService.getAllIdentitiesByUserId(userInfo.getId());
        List<IdentityType> identityTypes = identities.stream().map(IdentityType::forNumber).collect(Collectors.toList());
        return UserMessage.newBuilder()
                .setId(userInfo.getId().toString())
                .setName(userInfo.getNickName())
                .setPortraitImageUri(userInfo.getPortraitImageUri())
                .setGender(Gender.forNumber(userInfo.getGender()))
                .setDescription(userInfo.getDescription())
                .setCity(userInfo.getCity())
                .setBirthday(userInfo.getBirthday().toString())
                .setUnionId(userInfo.getUnionId())
                .addAllIdentities(identityTypes)
                .build();
    }
}
