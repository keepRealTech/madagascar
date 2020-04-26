package com.keepreal.madagascar.lemur.controller;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.lemur.dtoFactory.UserDTOFactory;
import com.keepreal.madagascar.lemur.service.LoginService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.LoginApi;
import swagger.model.*;

import javax.validation.Valid;
import java.util.Objects;

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
     * @param loginService   Login service.
     * @param userService    User service.
     * @param userDTOFactory User dto factory.
     */
    public LoginController(LoginService loginService,
                           UserService userService,
                           UserDTOFactory userDTOFactory) {
        this.loginService = loginService;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
    }

    /**
     * Overrides the login api.
     *
     * @param body {@link PostLoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    public ResponseEntity<LoginResponse> apiV1LoginPost(@Valid PostLoginRequest body) {
        LoginRequest loginRequest =
                LoginRequest.newBuilder()
                        .setCode(StringValue.of(body.getData()))
                        .setLoginType(this.loginTypeOf(body.getLoginType()))
                        .build();
        com.keepreal.madagascar.baobob.LoginResponse loginResponse = this.loginService.login(loginRequest);

        if (Objects.isNull(loginResponse)
                || !loginResponse.hasStatus()
                || ErrorCode.REQUEST_SUCC_VALUE != loginResponse.getStatus().getRtn()) {
            log.error(Objects.isNull(loginResponse) ? "GRpc login returned null." : loginResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID);
        }

        LoginResponse response = new LoginResponse();
        response.setData(this.buildTokenInfo(loginResponse));
        ResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Overrides the refresh token api.
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
     * Overrides the get user info api.
     *
     * @return {@link UserResponse}.
     */
    @Override
    public ResponseEntity<UserResponse> apiV1UserInfoGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        com.keepreal.madagascar.coua.UserResponse userResponse = this.userService.retrieveUserById(userId);

        if (Objects.isNull(userResponse)
                || !userResponse.hasStatus()
                || ErrorCode.REQUEST_SUCC_VALUE != userResponse.getStatus().getRtn()) {
            log.error(Objects.isNull(userResponse) ? "Retrieve user info returned null." : userResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_USER_NOT_FOUND_ERROR);
        }

        UserResponse response = new UserResponse();
        response.setData(this.userDTOFactory.valueOf(userResponse.getUser()));
        ResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Converts the {@link LoginType} into {@link com.keepreal.madagascar.common.LoginType}.
     *
     * @param loginType {@link LoginType}.
     * @return {@link com.keepreal.madagascar.common.LoginType}.
     */
    private com.keepreal.madagascar.common.LoginType loginTypeOf(LoginType loginType) {
        switch (loginType) {
            case WECHAT:
                return com.keepreal.madagascar.common.LoginType.LOGIN_OAUTH_WECHAT;
            default:
                throw new KeepRealBusinessException(ErrorCode.REQUEST_NOT_IMPLEMENTED_FUNCTION_ERROR);
        }
    }

    /**
     * Builds the {@link LoginTokenInfo} from a {@link com.keepreal.madagascar.baobob.LoginResponse}.
     *
     * @param loginResponse {@link com.keepreal.madagascar.baobob.LoginResponse}.
     * @return {@link LoginTokenInfo}.
     */
    private LoginTokenInfo buildTokenInfo(com.keepreal.madagascar.baobob.LoginResponse loginResponse) {
        LoginTokenInfo loginTokenInfo = new LoginTokenInfo();
        loginTokenInfo.setToken(loginResponse.getToken());
        loginTokenInfo.setRefreshToken(loginResponse.getRefreshToken());
        return loginTokenInfo;
    }

}
