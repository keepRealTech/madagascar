package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.IdentityType;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.coua.*;
import com.keepreal.madagascar.coua.dao.UserInfoRepository;
import com.keepreal.madagascar.coua.model.UserInfo;
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

    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private UserIdentityService userIdentityService;

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
        userInfo.setCreateTime(System.currentTimeMillis());
        userInfo.setUpdateTime(System.currentTimeMillis());

        saveAndResponse(userInfo, responseObserver);
    }

    @Override
    public void retrieveSingleUser(RetrieveSingleUserRequest request, StreamObserver<UserResponse> responseObserver) {
        QueryUserCondition queryUserCondition = request.getCondition();
        UserInfo userInfo = null;
        if (queryUserCondition.hasId()) {
            userInfo = userInfoRepository.findUserInfoById(Long.valueOf(queryUserCondition.getId().getValue()));
        }
        if (queryUserCondition.hasUnionId()) {
            userInfo = userInfoRepository.findUserInfoByUnionId(queryUserCondition.getUnionId().getValue());
        }
        if (userInfo == null) {
            throw new RuntimeException();//todo 是否有其他的处理办法
        }
        UserMessage userMessage = getUserMessage(userInfo);
        UserResponse userResponse = UserResponse.newBuilder().setUser(userMessage).build();
        responseObserver.onNext(userResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateUserById(UpdateUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        Long userId = Long.valueOf(request.getId());
        UserInfo userInfo = userInfoRepository.findUserInfoById(userId);
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
        return userInfoRepository.findUserInfoById(userId);
    }

    public UserMessage getUserMessageById(Long userId) {
        UserInfo userInfo = userInfoRepository.findUserInfoById(userId);
        if (userInfo == null) {
            // todo: throw exception?
        }
        return getUserMessage(userInfo);
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
