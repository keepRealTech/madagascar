package com.keepreal.madagascar.baobob.service;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.keepreal.madagascar.baobob.loginExecutor.model.IOSLoginInfo;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatUserInfo;
import com.keepreal.madagascar.common.GenderValue;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.NewUserRequest;
import com.keepreal.madagascar.coua.QueryUserCondition;
import com.keepreal.madagascar.coua.ReactorUserServiceGrpc;
import com.keepreal.madagascar.coua.RetrieveSingleUserRequest;
import com.keepreal.madagascar.coua.UpdateUserByIdRequest;
import com.keepreal.madagascar.coua.UserResponse;
import com.keepreal.madagascar.coua.UserState;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


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
     * Retrieves the user by union id.
     *
     * @param unionId Union id.
     * @return {@link UserMessage}.
     */
    public Mono<UserMessage> retrieveUserByUnionIdMono(String unionId) {
        ReactorUserServiceGrpc.ReactorUserServiceStub stub = ReactorUserServiceGrpc.newReactorStub(this.channel);

        QueryUserCondition condition = QueryUserCondition.newBuilder().setUnionId(StringValue.of(unionId)).build();
        RetrieveSingleUserRequest request = RetrieveSingleUserRequest.newBuilder().setCondition(condition).build();

        return stub.retrieveSingleUser(request)
                .filter(userResponse -> ErrorCode.REQUEST_SUCC_VALUE == (userResponse.getStatus().getRtn()))
                .map(UserResponse::getUser);
    }

    /**
     * Retrieves the user by username.
     *
     * @param username Username.
     * @return {@link UserMessage}.
     */
    public Mono<UserMessage> retrieveUserByUsernameMono(String username) {
        ReactorUserServiceGrpc.ReactorUserServiceStub stub = ReactorUserServiceGrpc.newReactorStub(this.channel);

        QueryUserCondition condition = QueryUserCondition.newBuilder().setUsername(StringValue.of(username)).build();
        RetrieveSingleUserRequest request = RetrieveSingleUserRequest.newBuilder().setCondition(condition).build();

        return stub.retrieveSingleUser(request).map(UserResponse::getUser);
    }

    /**
     * Retrieves the user by id.
     *
     * @param id Id.
     * @return {@link UserMessage}.
     */
    public Mono<UserMessage> retrieveUserByIdMono(String id) {
        ReactorUserServiceGrpc.ReactorUserServiceStub stub = ReactorUserServiceGrpc.newReactorStub(this.channel);

        QueryUserCondition condition = QueryUserCondition.newBuilder().setId(StringValue.of(id)).build();
        RetrieveSingleUserRequest request = RetrieveSingleUserRequest.newBuilder().setCondition(condition).build();

        return stub.retrieveSingleUser(request).map(UserResponse::getUser);
    }

    /**
     * Creates a new user by {@link WechatUserInfo}.
     *
     * @param wechatUserInfo {@link WechatUserInfo}.
     * @return {@link UserMessage}.
     */
    public Mono<UserMessage> createUserByWechatUserInfoMono(WechatUserInfo wechatUserInfo) {
        ReactorUserServiceGrpc.ReactorUserServiceStub stub = ReactorUserServiceGrpc.newReactorStub(this.channel);

        NewUserRequest request = NewUserRequest.newBuilder()
                .setName(StringValue.of(wechatUserInfo.getName()))
                .setGender(GenderValue.newBuilder().setValue(wechatUserInfo.getGender()))
                .setPortraitImageUri(StringValue.of(wechatUserInfo.getPortraitImageUri()))
                .setUnionId(wechatUserInfo.getUnionId())
                .build();

        return stub.createUser(request).map(UserResponse::getUser);
    }

    /**
     * Creates a new user by {@link IOSLoginInfo}.
     *
     * @param iosLoginInfo {@link IOSLoginInfo}.
     * @return {@link UserMessage}.
     */
    public Mono<UserMessage> createUserByIOSUserInfoMono(IOSLoginInfo iosLoginInfo) {
        ReactorUserServiceGrpc.ReactorUserServiceStub stub = ReactorUserServiceGrpc.newReactorStub(this.channel);

        NewUserRequest request = NewUserRequest.newBuilder()
                .setName(StringValue.of(iosLoginInfo.getFullName()))
                .setUnionId(iosLoginInfo.getUnionId())
                .build();

        return stub.createUser(request).map(UserResponse::getUser);
    }

    /**
     * update user state
     *
     * @param state {@link UserState}
     * @return {@link UserMessage}
     */
    public Mono<UserMessage> updateUserStateMono(String userId, Integer state) {
        ReactorUserServiceGrpc.ReactorUserServiceStub stub = ReactorUserServiceGrpc.newReactorStub(this.channel);

        UpdateUserByIdRequest request = UpdateUserByIdRequest.newBuilder()
                .setId(userId)
                .setState(Int32Value.of(state))
                .build();

        return stub.updateUserById(request)
                .filter(userResponse -> ErrorCode.REQUEST_SUCC_VALUE == (userResponse.getStatus().getRtn()))
                .map(UserResponse::getUser);
    }

    /**
     * Retrieves the user by mobile phone
     *
     * @param mobile mobile
     * @return {@link UserMessage}
     */
    public Mono<UserMessage> retrieveUserByMobileMono(String mobile) {
        ReactorUserServiceGrpc.ReactorUserServiceStub stub = ReactorUserServiceGrpc.newReactorStub(this.channel);
        QueryUserCondition condition = QueryUserCondition.newBuilder()
                .setMobile(StringValue.of(mobile))
                .build();
        RetrieveSingleUserRequest request = RetrieveSingleUserRequest.newBuilder().setCondition(condition).build();

        return stub.retrieveSingleUser(request)
                .filter(userResponse -> ErrorCode.REQUEST_SUCC_VALUE == (userResponse.getStatus().getRtn()))
                .map(UserResponse::getUser);
    }

    /**
     * 根据手机号创建新用户 (默认昵称, 默认头像)
     *
     * @param mobile 手机号
     * @param state  {@link UserState}
     * @return {@link UserMessage}
     */
    public Mono<UserMessage> createUserByMobileAndStateMono(String mobile, Integer state) {
        ReactorUserServiceGrpc.ReactorUserServiceStub stub = ReactorUserServiceGrpc.newReactorStub(this.channel);
        NewUserRequest request = NewUserRequest.newBuilder()
                .setMobile(StringValue.of(mobile))
                .setState(Int32Value.of(state))
                .build();
        return stub.createUser(request).map(UserResponse::getUser);
    }

}
