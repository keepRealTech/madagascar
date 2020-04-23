package com.keepreal.service;

import com.keepreal.dao.UserInfoRepository;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.IdentityType;
import com.keepreal.madagascar.coua.*;
import com.keepreal.model.UserInfo;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-22
 **/

@GRpcService
public class SystemUserServiceImpl extends SystemUserServiceGrpc.SystemUserServiceImplBase {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Override
    public void createSystemUser(NewSystemUserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserInfo userInfo = new UserInfo();
        userInfo.setNickName(request.getName().getValue());
        userInfo.setPortraitImageUri(request.getPortraitImageUri().getValue());
        userInfo.setGender(request.getGender().getValueValue());
        userInfo.setDescription(request.getDescription().getValue());
        userInfo.setCity(request.getCity().getValue());
        userInfo.setBirthday(Date.valueOf(request.getBirthday().getValue()));
        // todo: 向user_identity表中插入数据
        List<IdentityType> identitiesList = request.getIdentitiesList();
        UserInfo save = userInfoRepository.save(userInfo);

        UserMessage userMessage = getUserMessage(save);
        UserResponse userResponse = UserResponse.newBuilder().setSystemUser(userMessage).build();
        responseObserver.onNext(userResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveSingleSystemUser(RetrieveSingleSystemUserRequest request, StreamObserver<UserResponse> responseObserver) {
        String id = request.getCondition().getId();

        UserInfo userInfo = userInfoRepository.findByUserId(id);
        UserMessage userMessage = getUserMessage(userInfo);

        UserResponse userResponse = UserResponse.newBuilder().setSystemUser(userMessage).build();
        responseObserver.onNext(userResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateSystemUserById(UpdateSystemUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        UserInfo userInfo = userInfoRepository.findByUserId(request.getId());
        // todo: 如果是部分更新的话，需要判断request中每个参数是否为""，然后再set到userInfo对象中
        // 有没有更好的写法。。。
    }

    private UserMessage getUserMessage(UserInfo userInfo) {
        return UserMessage.newBuilder()
                .setId(userInfo.getUserId().toString())
                .setName(userInfo.getNickName())
                .setPortraitImageUri(userInfo.getPortraitImageUri())
                .setGender(Gender.forNumber(userInfo.getGender()))
                .setDescription(userInfo.getDescription())
                .setCity(userInfo.getCity())
                .setBirthday(userInfo.getBirthday().toString())
                // todo: 从user_identity表中拿到用户identity的list
                .build();
    }
}
