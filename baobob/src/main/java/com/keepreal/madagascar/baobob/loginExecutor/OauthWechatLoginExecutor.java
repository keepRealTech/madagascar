package com.keepreal.madagascar.baobob.loginExecutor;

import com.google.gson.Gson;
import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.config.wechat.OauthWechatLoginConfiguration;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatLoginInfo;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatUserInfo;
import com.keepreal.madagascar.baobob.service.ImageService;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * Represents a login executor working with wechat oauth.
 */
@Slf4j
public class OauthWechatLoginExecutor implements LoginExecutor {

    private final LocalTokenGranter tokenGranter;
    private final UserService userService;
    private final ImageService imageService;
    private final OauthWechatLoginConfiguration oauthWechatLoginConfiguration;
    private final GrpcResponseUtils grpcResponseUtils;
    private final Gson gson;

    /**
     * Constructs the executor.
     *
     * @param userservice                   {@link UserService}.
     * @param oauthWechatLoginConfiguration {@link OauthWechatLoginConfiguration}.
     * @param tokenGranter                  Token granter.
     * @param imageService                  {@link ImageService}.
     */
    OauthWechatLoginExecutor(UserService userservice,
                             OauthWechatLoginConfiguration oauthWechatLoginConfiguration,
                             LocalTokenGranter tokenGranter,
                             ImageService imageService) {
        this.userService = userservice;
        this.oauthWechatLoginConfiguration = oauthWechatLoginConfiguration;
        this.tokenGranter = tokenGranter;
        this.imageService = imageService;
        this.grpcResponseUtils = new GrpcResponseUtils();
        this.gson = new Gson();
    }

    /**
     * Overrides the login logic.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        if (!loginRequest.hasOauthWechatPayload()) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }

        return this.loginWechat(loginRequest.getOauthWechatPayload().getCode())
                .zipWhen(this::retrieveOrCreateUserByUnionId)
                .map(tuple -> this.tokenGranter.grant(tuple.getT2(), tuple.getT1().getOpenId()))
                .doOnError(error -> log.error(error.toString()))
                .onErrorReturn(throwable -> throwable instanceof KeepRealBusinessException
                                && ((KeepRealBusinessException) throwable).getErrorCode() == ErrorCode.REQUEST_GRPC_LOGIN_FROZEN,
                        this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN))
                .onErrorReturn(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
    }

    /**
     * Retrieves a user by union id, create a new one if not exists.
     *
     * @param wechatLoginInfo {@link WechatUserInfo}.
     * @return {@link UserMessage}.
     */
    private Mono<UserMessage> retrieveOrCreateUserByUnionId(WechatLoginInfo wechatLoginInfo) {
        assert !StringUtils.isEmpty(wechatLoginInfo.getUnionId());
        return this.userService.retrieveUserByUnionIdMono(wechatLoginInfo.getUnionId())
                .map(userMessage -> {
                    if (userMessage.getLocked()) {
                        throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN);
                    }
                    return userMessage;
                })
                .switchIfEmpty(this.createNewUserFromWechat(wechatLoginInfo));
    }

    /**
     * Creates a new user from wechat user info.
     *
     * @param wechatLoginInfo {@link WechatUserInfo}.
     * @return {@link UserMessage}.
     */
    private Mono<UserMessage> createNewUserFromWechat(WechatLoginInfo wechatLoginInfo) {
        return this.retrieveUserInfoFromWechat(wechatLoginInfo)
                .flatMap(this::migrateImage)
                .flatMap(this.userService::createUserByWechatUserInfoMono);
    }

    /**
     * Logs in wechat with code.
     *
     * @param code Code.
     * @return {@link WechatLoginInfo}.
     */
    @SuppressWarnings("unchecked")
    private Mono<WechatLoginInfo> loginWechat(String code) {
        String accessTokenUrl = String.format("%s/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                this.oauthWechatLoginConfiguration.getHost(),
                this.oauthWechatLoginConfiguration.getAppId(),
                this.oauthWechatLoginConfiguration.getAppSecret(),
                code);

        return WebClient.create(accessTokenUrl)
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> this.gson.fromJson(response, HashMap.class))
                .filter(map -> map.get("errcode") == null)
                .map(hashMap -> WechatLoginInfo.builder()
                        .accessToken(String.valueOf(hashMap.get("access_token")))
                        .openId(String.valueOf(hashMap.get("openid")))
                        .unionId(String.valueOf(hashMap.get("unionid")))
                        .build())
                .switchIfEmpty(Mono.error(new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID)));
    }

    /**
     * Retrieves the user info.
     *
     * @param wechatLoginInfo {@link WechatLoginInfo}.
     * @return {@link WechatUserInfo}.
     */
    @SuppressWarnings("unchecked")
    private Mono<WechatUserInfo> retrieveUserInfoFromWechat(WechatLoginInfo wechatLoginInfo) {
        String userInfoUrl = String.format("%s/sns/userinfo?access_token=%s&openid=%s",
                this.oauthWechatLoginConfiguration.getHost(),
                wechatLoginInfo.getAccessToken(),
                wechatLoginInfo.getOpenId());

        return WebClient.create(userInfoUrl)
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> this.gson.fromJson(response, HashMap.class))
                .map(hashMap ->
                        WechatUserInfo.builder()
                                .name(String.valueOf(hashMap.getOrDefault("nickname", "")))
                                .gender(this.convertGender(hashMap.getOrDefault("sex", 0)))
                                .province(String.valueOf(hashMap.getOrDefault("province", "")))
                                .city(String.valueOf(hashMap.getOrDefault("city", "")))
                                .country(String.valueOf(hashMap.getOrDefault("country", "")))
                                .portraitImageUri(String.valueOf(hashMap.getOrDefault("headimgurl", "")))
                                .unionId(String.valueOf(hashMap.get("unionid")))
                                .build());
    }

    /**
     * Converts wechat returned sex to {@link Gender}.
     *
     * @param wechatSex Wechat sex field.
     * @return {@link Gender}.
     */
    private Gender convertGender(Object wechatSex) {
        int sex = Double.valueOf(String.valueOf(wechatSex)).intValue();
        switch (sex) {
            case 1:
                return Gender.MALE;
            case 2:
                return Gender.FEMALE;
            default:
                return Gender.UNKNOWN;
        }
    }

    /**
     * Migrates the image from wechat to oss.
     *
     * @param wechatUserInfo {@link WechatUserInfo}.
     * @return {@link WechatUserInfo}.
     */
    private Mono<WechatUserInfo> migrateImage(WechatUserInfo wechatUserInfo) {
        if (StringUtils.isEmpty(wechatUserInfo.getPortraitImageUri())) {
            return Mono.just(wechatUserInfo);
        }

        return Mono.just(wechatUserInfo.getPortraitImageUri())
                .flatMap(this.imageService::migrateSingleImage)
                .map(uri -> {
                    wechatUserInfo.setPortraitImageUri(uri);
                    return wechatUserInfo;
                });
    }

}
