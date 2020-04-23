package com.keepreal.madagascar.baobob.loginExecutor;

import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.config.OauthWechatLoginConfiguration;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatLoginInfo;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatUserInfo;
import com.keepreal.madagascar.baobob.service.UserService;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.coua.UserMessage;
import com.keepreal.madagascar.error.ErrorCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * Represents a login executor working with wechat oauth.
 */
public class OauthWechatLoginExecutor implements LoginExecutor {

    public final LocalTokenGranter tokenGranter;
    private final UserService userService;
    private final OauthWechatLoginConfiguration oauthWechatLoginConfiguration;
    private final GrpcResponseUtils grpcResponseUtils;

    /**
     * Constructs the executor.
     *
     * @param userservice                   {@link UserService}.
     * @param oauthWechatLoginConfiguration {@link OauthWechatLoginConfiguration}.
     * @param tokenGranter                  Token granter.
     */
    OauthWechatLoginExecutor(UserService userservice,
                             OauthWechatLoginConfiguration oauthWechatLoginConfiguration,
                             LocalTokenGranter tokenGranter) {
        this.userService = userservice;
        this.oauthWechatLoginConfiguration = oauthWechatLoginConfiguration;
        this.tokenGranter = tokenGranter;
        this.grpcResponseUtils = new GrpcResponseUtils();
    }

    /**
     * Overrides the login logic.
     *
     * @param loginRequest Login request {@link LoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        if (!loginRequest.hasCode()) {
            return Mono.just(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.GRPC_LOGIN_INVALID));
        }

        return this.loginWechat(loginRequest.getCode().getValue())
                .flatMap(this::retrieveOrCreateUserByUnionId)
                .map(this.tokenGranter::grant);
    }

    /**
     * Retrieves a user by union id, create a new one if not exists.
     *
     * @param wechatLoginInfo {@link WechatUserInfo}.
     * @return {@link UserMessage}.
     */
    private Mono<UserMessage> retrieveOrCreateUserByUnionId(WechatLoginInfo wechatLoginInfo) {
        return this.userService.retrieveUserByUnionIdMono(wechatLoginInfo.getUnionId())
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
                .bodyToMono(HashMap.class)
                .map(hashMap ->
                        WechatLoginInfo.builder()
                                .accessToken(String.valueOf(hashMap.get("access_token")))
                                .openId(String.valueOf(hashMap.get("openid")))
                                .unionId(String.valueOf(hashMap.get("unionid")))
                                .build());
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
                .bodyToMono(HashMap.class)
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
        int sex = Integer.parseInt(String.valueOf(wechatSex));
        switch (sex) {
            case 1:
                return Gender.MALE;
            case 2:
                return Gender.FEMALE;
            default:
                return Gender.UNKNOWN;
        }
    }

}
