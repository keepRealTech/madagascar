package com.keepreal.madagascar.coua.grpcController;

import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.NewUserRequest;
import com.keepreal.madagascar.coua.QueryUserCondition;
import com.keepreal.madagascar.coua.RetrieveSingleUserRequest;
import com.keepreal.madagascar.coua.UpdateUserByIdRequest;
import com.keepreal.madagascar.coua.UserResponse;
import com.keepreal.madagascar.coua.UserServiceGrpc;
import com.keepreal.madagascar.coua.model.UserInfo;
import com.keepreal.madagascar.coua.service.UserIdentityService;
import com.keepreal.madagascar.coua.service.UserInfoService;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.util.StringUtils;

import java.sql.Date;

/**
 * Represents user GRpc controller.
 */
@GRpcService
public class UserGRpcController extends UserServiceGrpc.UserServiceImplBase {

    private final UserInfoService userInfoService;
    private final UserIdentityService userIdentityService;

    /**
     * Constructs user grpc controller.
     *
     * @param userInfoService       {@link UserInfoService}.
     * @param userIdentityService   {@link UserIdentityService}.
     */
    public UserGRpcController(UserInfoService userInfoService,
                              UserIdentityService userIdentityService) {
        this.userInfoService = userInfoService;
        this.userIdentityService = userIdentityService;
    }

    /**
     * Implements create user method.
     *
     * @param request           {@link NewUserRequest}.
     * @param responseObserver  {@link UserResponse}.
     */
    @Override
    public void createUser(NewUserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserInfo userInfo = UserInfo.builder()
                .nickName(request.getName().getValue())
                .portraitImageUri(request.getPortraitImageUri().getValue())
                .gender(request.getGender().getValueValue())
                .city(request.getCity().getValue())
                .description(request.getDescription().getValue())
                .unionId(request.getUnionId())
                .build();

        if (request.hasBirthday()) {
            String birthdayStr = request.getBirthday().getValue();
            if (!StringUtils.isEmpty(birthdayStr)) {
                userInfo.setBirthday(Date.valueOf(birthdayStr));
            }
        }
        basicResponse(responseObserver, userInfoService.createUser(userInfo));
    }

    /**
     * Implements retrieve single user method.
     *
     * @param request           {@link RetrieveSingleUserRequest}. (userId, unionId, displayId)
     * @param responseObserver  {@link UserResponse}.
     */
    @Override
    public void retrieveSingleUser(RetrieveSingleUserRequest request, StreamObserver<UserResponse> responseObserver) {
        QueryUserCondition queryUserCondition = request.getCondition();
        UserInfo userInfo = null;
        UserResponse.Builder responseBuilder = UserResponse.newBuilder();
        if (queryUserCondition.hasId()) {
            userInfo = userInfoService.findUserInfoByIdAndDeletedIsFalse(queryUserCondition.getId().getValue());
        }
        if (queryUserCondition.hasUnionId()) {
            userInfo = userInfoService.findUserInfoByUnionIdAndDeletedIsFalse(queryUserCondition.getUnionId().getValue());
        }
        if (queryUserCondition.hasDisplayId()) {
            userInfo = userInfoService.findUserInfoByDisplayIdAndDeletedIsFalse(queryUserCondition.getDisplayId().getValue());
        }
        if (queryUserCondition.hasUsername()) {
            userInfo = userInfoService.findUserInfoByUserNameAndDeletedIsFalse(queryUserCondition.getUsername().getValue());
        }
        if (userInfo == null) {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_NOT_FOUND_ERROR);
            responseBuilder.setStatus(commonStatus);
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        } else {
            UserMessage userMessage = userInfoService.getUserMessage(userInfo);
            responseBuilder.setUser(userMessage);
        }
        UserResponse userResponse = responseBuilder.setStatus(CommonStatusUtils.getSuccStatus()).build();
        responseObserver.onNext(userResponse);
        responseObserver.onCompleted();
    }

    /**
     * Implements update user by id method.
     *
     * @param request           {@link UpdateUserByIdRequest}.
     * @param responseObserver  {@link UserResponse}.
     */
    @Override
    public void updateUserById(UpdateUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        String userId = request.getId();
        UserInfo userInfo = userInfoService.findUserInfoByIdAndDeletedIsFalse(userId);
        if (userInfo == null) {
            CommonStatus commonStatus = CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_NOT_FOUND_ERROR);
            responseObserver.onNext(UserResponse.newBuilder().setStatus(commonStatus).build());
            responseObserver.onCompleted();
            return;
        }
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
            userIdentityService.updateUserIdentities(request.getIdentitiesValueList(), userId);
        }
        if (request.hasDisplayId()) {
            userInfo.setDisplayId(request.getDisplayId().getValue());
        }

        basicResponse(responseObserver, userInfoService.updateUser(userInfo));
    }

    /**
     * basic response.
     *
     * @param responseObserver {@link UserResponse}.
     * @param userInfo         {@link UserInfo}.
     */
    private void basicResponse(StreamObserver<UserResponse> responseObserver, UserInfo userInfo) {
        UserResponse userResponse = UserResponse.newBuilder()
                .setUser(userInfoService.getUserMessage(userInfo))
                .setStatus(CommonStatusUtils.getSuccStatus()).build();
        responseObserver.onNext(userResponse);
        responseObserver.onCompleted();
    }
}
