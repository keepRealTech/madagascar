package com.keepreal.madagascar.lemur.controller;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.OAuthWechatLoginPayload;
import com.keepreal.madagascar.baobob.PasswordLoginPayload;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.dtoFactory.UserDTOFactory;
import com.keepreal.madagascar.lemur.service.LoginService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.LoginApi;
import swagger.model.LoginResponse;
import swagger.model.LoginTokenInfo;
import swagger.model.LoginType;
import swagger.model.PostLoginRequest;
import swagger.model.PostRefreshTokenRequest;
import swagger.model.UserResponse;

import javax.validation.Valid;

import static swagger.model.LoginType.OAUTH_WECHAT;

/**
 * Represents the login controllers.
 */
@RestController
@Slf4j
public class LoginController implements LoginApi {

    private final LoginService loginService;
    private final UserService userService;
    private final UserDTOFactory userDTOFactory;

    /**
     * Constructs the login controller.
     *
     * @param loginService   {@link LoginService}.
     * @param userService    {@link UserService}.
     * @param userDTOFactory {@link UserDTOFactory}.
     */
    public LoginController(LoginService loginService,
                           UserService userService,
                           UserDTOFactory userDTOFactory) {
        this.loginService = loginService;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
    }

    /**
     * Implements the login api.
     *
     * @param body {@link PostLoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public ResponseEntity<LoginResponse> apiV1LoginPost(@Valid PostLoginRequest body) {
        LoginRequest loginRequest;
        switch (body.getLoginType()) {
            case OAUTH_WECHAT:
                loginRequest = LoginRequest.newBuilder()
                    .setOauthWechatPayload(OAuthWechatLoginPayload.newBuilder()
                            .setCode(body.getData().getCode()))
                    .setLoginType(com.keepreal.madagascar.common.LoginType.LOGIN_OAUTH_WECHAT)
                    .build();
                break;
            case PASSWORD:
                loginRequest = LoginRequest.newBuilder()
                        .setPasswordPayload(PasswordLoginPayload.newBuilder()
                                .setUsername(body.getData().getUsername())
                                .setPassword(body.getData().getPassword()))
                        .setLoginType(com.keepreal.madagascar.common.LoginType.LOGIN_PASSWORD)
                        .build();
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        com.keepreal.madagascar.baobob.LoginResponse loginResponse = this.loginService.login(loginRequest);

        UserMessage user = this.userService.retrieveUserById(loginResponse.getUserId());

        LoginResponse response = new LoginResponse();
        response.setData(this.buildTokenInfo(loginResponse, user));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the refresh token api.
     *
     * @param body {@link PostRefreshTokenRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public ResponseEntity<LoginResponse> apiV1RefreshTokenPost(@Valid PostRefreshTokenRequest body) {
        this.loginService.refresh(body.getRefreshToken());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Implements the get user info api.
     *
     * @return {@link UserResponse}.
     */
    @Override
    public ResponseEntity<UserResponse> apiV1UserInfoGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        UserMessage user = this.userService.retrieveUserById(userId);

        UserResponse response = new UserResponse();
        response.setData(this.userDTOFactory.valueOf(user));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Builds the {@link LoginTokenInfo} from a {@link com.keepreal.madagascar.baobob.LoginResponse}.
     *
     * @param loginResponse {@link com.keepreal.madagascar.baobob.LoginResponse}.
     * @param userMessage   {@link UserMessage}.
     * @return {@link LoginTokenInfo}.
     */
    private LoginTokenInfo buildTokenInfo(com.keepreal.madagascar.baobob.LoginResponse loginResponse,
                                          UserMessage userMessage) {
        LoginTokenInfo loginTokenInfo = new LoginTokenInfo();
        loginTokenInfo.setToken(loginResponse.getToken());
        loginTokenInfo.setRefreshToken(loginResponse.getRefreshToken());
        loginTokenInfo.setUser(this.userDTOFactory.valueOf(userMessage));
        return loginTokenInfo;
    }

}
