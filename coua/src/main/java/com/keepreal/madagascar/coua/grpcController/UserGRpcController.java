package com.keepreal.madagascar.coua.grpcController;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.DeviceType;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.coua.CheckUserMobileIsExistedRequest;
import com.keepreal.madagascar.coua.CheckUserMobileIsExistedResponse;
import com.keepreal.madagascar.coua.CreateOrUpdateUserPasswordRequest;
import com.keepreal.madagascar.coua.CreateOrUpdateUserQualificationsRequest;
import com.keepreal.madagascar.coua.CreateOrUpdateUserQualificationsResponse;
import com.keepreal.madagascar.coua.DeviceTokenRequest;
import com.keepreal.madagascar.coua.DeviceTokenResponse;
import com.keepreal.madagascar.coua.NewUserRequest;
import com.keepreal.madagascar.coua.QualificationMessage;
import com.keepreal.madagascar.coua.QueryUserCondition;
import com.keepreal.madagascar.coua.RetreiveMultipleUsersByIdsRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokenResponse;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensByUserIdListRequest;
import com.keepreal.madagascar.coua.RetrieveDeviceTokensByUserIdListResponse;
import com.keepreal.madagascar.coua.RetrieveSingleUserRequest;
import com.keepreal.madagascar.coua.RetrieveUserQualificationsRequest;
import com.keepreal.madagascar.coua.RetrieveUserQualificationsResponse;
import com.keepreal.madagascar.coua.SendOtpToMobileRequest;
import com.keepreal.madagascar.coua.SendOtpToMobileResponse;
import com.keepreal.madagascar.coua.UpdateUserByIdRequest;
import com.keepreal.madagascar.coua.UpdateUserMobileRequest;
import com.keepreal.madagascar.coua.UserResponse;
import com.keepreal.madagascar.coua.UserServiceGrpc;
import com.keepreal.madagascar.coua.UserState;
import com.keepreal.madagascar.coua.UsersReponse;
import com.keepreal.madagascar.coua.model.QualificationState;
import com.keepreal.madagascar.coua.model.SimpleDeviceToken;
import com.keepreal.madagascar.coua.model.UserInfo;
import com.keepreal.madagascar.coua.model.UserQualification;
import com.keepreal.madagascar.coua.service.AliyunSmsService;
import com.keepreal.madagascar.coua.service.ChatService;
import com.keepreal.madagascar.coua.service.TransactionProducerService;
import com.keepreal.madagascar.coua.service.UserDeviceInfoService;
import com.keepreal.madagascar.coua.service.UserEventProducerService;
import com.keepreal.madagascar.coua.service.UserIdentityService;
import com.keepreal.madagascar.coua.service.UserInfoService;
import com.keepreal.madagascar.coua.service.UserQualificationService;
import com.keepreal.madagascar.coua.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.keepreal.madagascar.coua.service.AliyunSmsService.MOBILE_PHONE_OTP;

/**
 * Represents user GRpc controller.
 */
@Slf4j
@GRpcService
public class UserGRpcController extends UserServiceGrpc.UserServiceImplBase {

    private final ChatService chatService;
    private final UserInfoService userInfoService;
    private final UserDeviceInfoService userDeviceInfoService;
    private final UserIdentityService userIdentityService;
    private final AliyunSmsService aliyunSmsService;
    private final RedissonClient redissonClient;
    private final TransactionProducerService transactionProducerService;
    private final UserEventProducerService userEventProducerService;
    private final UserQualificationService userQualificationService;
    private final BCryptPasswordEncoder encoder;

    /**
     * Constructs user grpc controller.
     *
     * @param chatService                {@link ChatService}.
     * @param userInfoService            {@link UserInfoService}.
     * @param userDeviceInfoService      {@link UserDeviceInfoService}.
     * @param userIdentityService        {@link UserIdentityService}.
     * @param aliyunSmsService           {@link AliyunSmsService}
     * @param redissonClient             {@link RedissonClient}.
     * @param transactionProducerService {@link TransactionProducerService}.
     * @param userQualificationService   {@link UserQualificationService}.
     */
    public UserGRpcController(ChatService chatService,
                              UserInfoService userInfoService,
                              UserDeviceInfoService userDeviceInfoService,
                              UserIdentityService userIdentityService,
                              AliyunSmsService aliyunSmsService,
                              RedissonClient redissonClient,
                              TransactionProducerService transactionProducerService,
                              UserEventProducerService userEventProducerService,
                              UserQualificationService userQualificationService) {
        this.chatService = chatService;
        this.userInfoService = userInfoService;
        this.userDeviceInfoService = userDeviceInfoService;
        this.userIdentityService = userIdentityService;
        this.aliyunSmsService = aliyunSmsService;
        this.redissonClient = redissonClient;
        this.transactionProducerService = transactionProducerService;
        this.userEventProducerService = userEventProducerService;
        this.userQualificationService = userQualificationService;
        this.encoder = new BCryptPasswordEncoder();
    }

    /**
     * Implements create user method.
     *
     * @param request          {@link NewUserRequest}.
     * @param responseObserver {@link UserResponse}.
     */
    @Override
    public void createUser(NewUserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserInfo userInfo = UserInfo.builder()
                .nickName(request.getName().getValue())
                .portraitImageUri(request.getPortraitImageUri().getValue())
                .gender(request.hasGender() ? request.getGender().getValueValue() : Gender.UNSET_VALUE)
                .city(request.getCity().getValue())
                .description(request.getDescription().getValue())
                .unionId(request.getUnionId())
                .mobile(request.getMobile().getValue())
                .build();

        if (request.hasBirthday()) {
            String birthdayStr = request.getBirthday().getValue();
            if (!StringUtils.isEmpty(birthdayStr)) {
                userInfo.setBirthday(Date.valueOf(birthdayStr));
            }
        }

        if (request.hasState()) {
            userInfo.setState(request.getState().getValue());
        }

        UserInfo user = userInfoService.createUser(userInfo);
        this.userEventProducerService.produceCreateUserEventAsync(user.getId());
        basicResponse(responseObserver, user);
    }

    /**
     * Implements retrieve single user method.
     *
     * @param request          {@link RetrieveSingleUserRequest}. (userId, unionId, displayId)
     * @param responseObserver {@link UserResponse}.
     */
    @Override
    public void retrieveSingleUser(RetrieveSingleUserRequest request, StreamObserver<UserResponse> responseObserver) {
        QueryUserCondition queryUserCondition = request.getCondition();
        UserInfo userInfo = null;
        String condition = "";
        UserResponse.Builder responseBuilder = UserResponse.newBuilder();
        if (queryUserCondition.hasId()) {
            condition = queryUserCondition.getId().getValue();
            userInfo = this.userInfoService.findUserInfoByIdAndDeletedIsFalse(condition);
        }
        if (queryUserCondition.hasUnionId()) {
            condition = queryUserCondition.getUnionId().getValue();
            userInfo = this.userInfoService.findUserInfoByUnionIdAndDeletedIsFalse(condition);
        }
        if (queryUserCondition.hasDisplayId()) {
            condition = queryUserCondition.getDisplayId().getValue();
            userInfo = this.userInfoService.findUserInfoByDisplayIdAndDeletedIsFalse(condition);
        }
        if (queryUserCondition.hasUsername()) {
            condition = queryUserCondition.getUsername().getValue();
            userInfo = this.userInfoService.findUserInfoByUserNameAndDeletedIsFalse(condition);
        }
        if (queryUserCondition.hasMobile() && queryUserCondition.hasState()) {
            condition = queryUserCondition.getMobile().getValue();
            userInfo = this.userInfoService.findUserByMobileAndState(condition, queryUserCondition.getState().getValue());
        }
        if (queryUserCondition.hasMobile() && !queryUserCondition.hasState()) {
            condition = queryUserCondition.getMobile().getValue();
            userInfo = this.userInfoService.findUserInfoByMobile(condition);
        }
        if (userInfo == null) {
            log.error("[retrieveSingleUser] user not found error! condition is [{}]", condition);
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
     * @param request          {@link UpdateUserByIdRequest}.
     * @param responseObserver {@link UserResponse}.
     */
    @Override
    public void updateUserById(UpdateUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        String userId = request.getId();
        UserInfo userInfo = userInfoService.findUserInfoByIdAndDeletedIsFalse(userId);
        if (userInfo == null) {
            log.error("[updateUserById] user not found error! condition is [{}]", userId);
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
        if (request.hasState()) {
            userInfo.setState(request.getState().getValue());
        }

        basicResponse(responseObserver, userInfoService.updateUser(userInfo));

        if (request.hasName() || request.hasPortraitImageUri()) {
            this.chatService.updateRongCloudUserInfo(request.getId(),
                    userInfo.getNickName(),
                    userInfo.getPortraitImageUri());
        }
    }

    /**
     * Implements update device token method.
     *
     * @param request          {@link DeviceTokenRequest}.
     * @param responseObserver {@link DeviceTokenResponse}.
     */
    @Override
    public void updateDeviceToken(DeviceTokenRequest request, StreamObserver<DeviceTokenResponse> responseObserver) {
        if (request.getIsBind()) {
            userDeviceInfoService.bindDeviceToken(request.getUserId(), request.getDeviceToken(), request.getDeviceTypeValue());
        } else {
            userDeviceInfoService.unbindDeviceToken(request.getUserId(), request.getDeviceToken(), request.getDeviceTypeValue());
        }

        responseObserver.onNext(DeviceTokenResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build());
        responseObserver.onCompleted();
    }

    /**
     * Implements the retrieve device token by user id method.
     *
     * @param request
     * @param responseObserver
     */
    @Override
    public void retrieveDeviceTokenByUserId(RetrieveDeviceTokenRequest request, StreamObserver<RetrieveDeviceTokenResponse> responseObserver) {
        String userId = request.getUserId();
        List<SimpleDeviceToken> userDeviceInfos = userDeviceInfoService.getDeviceTokenByUserId(userId);
        List<String> androidTokenList = new ArrayList<>();
        List<String> iosTokenList = new ArrayList<>();
        userDeviceInfos.forEach(info -> {
            if (info.getDeviceType().equals(DeviceType.ANDROID_VALUE)) {
                androidTokenList.add(info.getDeviceToken());
            } else {
                iosTokenList.add(info.getDeviceToken());
            }
        });
        responseObserver.onNext(RetrieveDeviceTokenResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllAndroidTokens(androidTokenList)
                .addAllIosTokens(iosTokenList)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveDeviceTokensByUserIdList(RetrieveDeviceTokensByUserIdListRequest request, StreamObserver<RetrieveDeviceTokensByUserIdListResponse> responseObserver) {
        ProtocolStringList userIdsList = request.getUserIdsList();

        List<SimpleDeviceToken> tokenList = userDeviceInfoService.getDeviceTokenListByUserIdList(userIdsList);

        List<String> androidTokenList = new ArrayList<>();
        List<String> iosTokenList = new ArrayList<>();

        tokenList.forEach(info -> {
            if (info.getDeviceType().equals(DeviceType.ANDROID_VALUE)) {
                androidTokenList.add(info.getDeviceToken());
            } else {
                iosTokenList.add(info.getDeviceToken());
            }
        });

        RetrieveDeviceTokensByUserIdListResponse response = RetrieveDeviceTokensByUserIdListResponse.newBuilder()
                .addAllAndroidTokens(androidTokenList)
                .addAllIosTokens(iosTokenList)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the retrieve users by ids.
     *
     * @param request          {@link RetreiveMultipleUsersByIdsRequest}.
     * @param responseObserver {@link StreamObserver}.
     */
    @Override
    public void retrieveUsersByIds(RetreiveMultipleUsersByIdsRequest request,
                                   StreamObserver<UsersReponse> responseObserver) {
        List<UserInfo> userInfos = this.userInfoService.findUserInfosByIds(request.getUserIdsList());

        UsersReponse response = UsersReponse.newBuilder()
                .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .addAllUsers(userInfos.stream()
                        .map(this.userInfoService::getUserMessage)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Implements the send otp to mobile phone .
     *
     * @param request          {@link SendOtpToMobileRequest}
     * @param responseObserver {@link StreamObserver}
     */
    @Override
    public void sendOtpToMobile(SendOtpToMobileRequest request, StreamObserver<SendOtpToMobileResponse> responseObserver) {
        CommonStatus commonStatus = aliyunSmsService.sendOtpToMobile(request.getCode(), request.getMobile());
        SendOtpToMobileResponse response = SendOtpToMobileResponse.newBuilder().setStatus(commonStatus).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 更新当前用户手机号 (如果手机号有H5注册登录信息则进行账号合并)
     *
     * @param request          {@link UpdateUserMobileRequest}
     * @param responseObserver {@link StreamObserver}
     */
    @Override
    public void updateUserMobile(UpdateUserMobileRequest request, StreamObserver<UserResponse> responseObserver) {
        String userId = request.getUserId();
        String mobile = request.getMobile();
        Integer otp = request.getOtp();
        String code = request.getCode();
        UserResponse.Builder builder = UserResponse.newBuilder();

        RBucket<Integer> redisOtp = this.redissonClient.getBucket(MOBILE_PHONE_OTP + code + "-" + mobile);
        Integer intOtp = redisOtp.get();

        if (Objects.isNull(intOtp) || !otp.equals(intOtp)) {
            builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_MOBILE_OTP_NOT_MATCH));
        } else {
            UserInfo h5UserInfo = this.userInfoService.findUserByMobileAndState(code + "-" + mobile, UserState.USER_H5_MOBILE_VALUE);
            if (Objects.nonNull(h5UserInfo)) {
                this.mergeUserAccounts(userId, h5UserInfo.getId());
            }
            UserInfo userInfo = this.userInfoService.findUserInfoByIdAndDeletedIsFalse(userId);
            userInfo.setMobile(code + "-" +mobile);
            userInfo.setUsername(code + "-" + mobile);
            UserInfo userInfoNew = this.userInfoService.updateUser(userInfo);
            builder.setStatus(CommonStatusUtils.getSuccStatus());
            builder.setUser(this.userInfoService.getUserMessage(userInfoNew));
            redisOtp.delete();
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * 判断手机号是否已经被绑定
     *
     * @param request          {@link CheckUserMobileIsExistedRequest}
     * @param responseObserver {@link StreamObserver}
     */
    @Override
    public void checkUserMobileIsExisted(CheckUserMobileIsExistedRequest request, StreamObserver<CheckUserMobileIsExistedResponse> responseObserver) {
        String mobile = request.getMobile();
        String userId = request.getUserId();
        String code = request.getCode();
        CheckUserMobileIsExistedResponse.Builder builder = CheckUserMobileIsExistedResponse.newBuilder();

        UserInfo userInfo = this.userInfoService.findUserInfoByMobile(code + "-" + mobile);

        if (Objects.nonNull(userInfo)) {
            if (userInfo.getState() == UserState.USER_WECHAT_VALUE) {
                if (userId.equals(userInfo.getId())) {
                    responseObserver.onNext(builder
                            .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_MOBILE_BOUND)).build());
                } else {
                    responseObserver.onNext(builder
                            .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_MOBILE_EXISTED)).build());
                }
            } else if (userInfo.getState() == UserState.USER_APP_MOBILE_VALUE) {
                responseObserver.onNext(builder
                        .setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_MOBILE_REGISTERED)).build());
            } else {
                responseObserver.onNext(builder
                        .setStatus(CommonStatusUtils.getSuccStatus()).build());
            }
            responseObserver.onCompleted();
            return;
        }

        responseObserver.onNext(builder
                .setStatus(CommonStatusUtils.getSuccStatus()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveUserQualifications(RetrieveUserQualificationsRequest request, StreamObserver<RetrieveUserQualificationsResponse> responseObserver) {
        String userId = request.getUserId();
        List<UserQualification> userQualifications = this.userQualificationService.retrieveUserQualificationsByUserId(userId);

        responseObserver.onNext(RetrieveUserQualificationsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllMessage(userQualifications.stream().map(this.userQualificationService::getMessage).collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void createOrUpdateUserQualifications(CreateOrUpdateUserQualificationsRequest request, StreamObserver<CreateOrUpdateUserQualificationsResponse> responseObserver) {
        String userId = request.getUserId();
        List<QualificationMessage> messageList = request.getMessageList();

        List<UserQualification> userQualifications = this.userQualificationService.retrieveUserQualificationsByUserId(userId);
        Map<String, UserQualification> userQualificationMap = userQualifications.stream().collect(Collectors.toMap(
                UserQualification::getId,
                userQualification -> {
                    userQualification.setDeleted(true);
                    return userQualification;
                },
                (k1, k2) -> k1));

        List<UserQualification> list = new ArrayList<>();
        messageList.forEach(message -> {
            if (StringUtils.isEmpty(message.getId()) && !StringUtils.isEmpty(message.getName()) && !StringUtils.isEmpty(message.getUrl())) {
                UserQualification userQualification = new UserQualification();
                userQualification.setUserId(userId);
                userQualification.setName(message.getName());
                userQualification.setHostUrl(message.getUrl());
                userQualification.setState(QualificationState.PROCESSING.getValue());
                list.add(userQualification);
            } else {
                UserQualification userQualification = userQualificationMap.get(message.getId());
                if (!userQualification.getHostUrl().equals(message.getUrl()) || !userQualification.getName().equals(message.getName())) {
                    userQualification.setState(QualificationState.PROCESSING.getValue());
                    userQualification.setHostUrl(message.getUrl());
                    userQualification.setName(message.getName());
                }
                userQualification.setDeleted(false);
            }
        });
        list.addAll(userQualificationMap.values());
        List<UserQualification> qualifications = this.userQualificationService.createOrUpdateQualifications(list);

        responseObserver.onNext(CreateOrUpdateUserQualificationsResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .addAllMessage(qualifications.stream().filter(userQualification -> !userQualification.getDeleted()).map(this.userQualificationService::getMessage).collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }

    /**
     * 创建/更新 用户密码
     *
     * @param request   {@link CreateOrUpdateUserPasswordRequest}
     * @param responseObserver  {@link UserResponse}
     */
    @Override
    public void createOrUpdateUserPassword(CreateOrUpdateUserPasswordRequest request, StreamObserver<UserResponse> responseObserver) {
        String userId = request.getUserId();
        String code = request.getCode();
        String mobile = request.getMobile();
        Integer otp = request.getOtp();
        String password = request.getPassword();

        UserResponse.Builder responseBuilder = UserResponse.newBuilder();

        RBucket<Integer> bucket = this.redissonClient.getBucket(MOBILE_PHONE_OTP + code + "-" + mobile);
        if (!bucket.isExists() || !bucket.get().equals(otp)) {
            responseBuilder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_USER_MOBILE_OTP_NOT_MATCH));
        } else {
            bucket.delete();
            UserInfo userInfo = this.userInfoService.findUserInfoByIdAndDeletedIsFalse(userId);
            userInfo.setPassword(this.encoder.encode(password));
            UserInfo userInfoNew = this.userInfoService.updateUser(userInfo);
            responseBuilder.setStatus(CommonStatusUtils.getSuccStatus());
            responseBuilder.setUser(this.userInfoService.getUserMessage(userInfoNew));
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    /**
     * 合并用户账户信息
     *
     * @param wechatUserId    wechat user id
     * @param webMobileUserId web user id
     */
    private void mergeUserAccounts(String wechatUserId, String webMobileUserId) {
        this.transactionProducerService.produceMergeUserAccountsTransactionEventAsync(wechatUserId, webMobileUserId);
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
