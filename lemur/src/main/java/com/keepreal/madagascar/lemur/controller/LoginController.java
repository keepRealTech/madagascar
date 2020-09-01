package com.keepreal.madagascar.lemur.controller;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.keepreal.madagascar.baobob.CheckSignatureRequest;
import com.keepreal.madagascar.baobob.GenerateQrcodeResponse;
import com.keepreal.madagascar.baobob.JWTISOLoginPayload;
import com.keepreal.madagascar.baobob.LoginRequest;
import com.keepreal.madagascar.baobob.OAuthWechatLoginPayload;
import com.keepreal.madagascar.baobob.PasswordLoginPayload;
import com.keepreal.madagascar.baobob.TokenRefreshPayload;
import com.keepreal.madagascar.baobob.WebMobileLoginPayload;
import com.keepreal.madagascar.brookesia.StatsEventAction;
import com.keepreal.madagascar.brookesia.StatsEventCategory;
import com.keepreal.madagascar.common.LoginType;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.stats_events.annotation.HttpStatsEventTrigger;
import com.keepreal.madagascar.lemur.config.OssClientConfiguration;
import com.keepreal.madagascar.lemur.dtoFactory.UserDTOFactory;
import com.keepreal.madagascar.lemur.service.LoginService;
import com.keepreal.madagascar.lemur.service.UserService;
import com.keepreal.madagascar.lemur.util.DummyResponseUtils;
import com.keepreal.madagascar.lemur.util.HttpContextUtils;
import com.keepreal.madagascar.lemur.util.WXPayUtil;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import swagger.api.LoginApi;
import swagger.model.BriefTokenInfo;
import swagger.model.DeviceTokenRequest;
import swagger.model.DummyResponse;
import swagger.model.LoginResponse;
import swagger.model.LoginTokenInfo;
import swagger.model.OssTokenDTO;
import swagger.model.OssTokenResponse;
import swagger.model.PostLoginRequest;
import swagger.model.PostOTPRequest;
import swagger.model.PostRefreshTokenRequest;
import swagger.model.QrTicketDTO;
import swagger.model.QrTicketResponse;
import swagger.model.RefreshTokenResponse;
import swagger.model.UserResponse;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.Map;
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
    private final OssClientConfiguration ossClientConfiguration;
    private final DefaultAcsClient acsClient;

    /**
     * Constructs the login controller.
     *
     * @param loginService           {@link LoginService}.
     * @param userService            {@link UserService}.
     * @param userDTOFactory         {@link UserDTOFactory}.
     * @param ossClientConfiguration {@link OssClientConfiguration}.
     * @param acsClient              {@link DefaultAcsClient}.
     */
    public LoginController(LoginService loginService,
                           UserService userService,
                           UserDTOFactory userDTOFactory,
                           OssClientConfiguration ossClientConfiguration,
                           @Qualifier("oss-acs-client") DefaultAcsClient acsClient) {
        this.loginService = loginService;
        this.userService = userService;
        this.userDTOFactory = userDTOFactory;
        this.ossClientConfiguration = ossClientConfiguration;
        this.acsClient = acsClient;
    }

    /**
     * Implements the login api.
     *
     * @param body {@link PostLoginRequest}.
     * @return {@link LoginResponse}.
     */
    @Override
    @HttpStatsEventTrigger(
            category = StatsEventCategory.STATS_CAT_LOGIN,
            action = StatsEventAction.STATS_ACT_NONE,
            label = "user id",
            value = "body.data.user.id"
    )
    @CrossOrigin
    public ResponseEntity<LoginResponse> apiV1LoginPost(@Valid PostLoginRequest body) {
        LoginRequest loginRequest;
        switch (body.getLoginType()) {
            case OAUTH_WECHAT:
                if (StringUtils.isEmpty(body.getData().getCode())) {
                    throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
                }

                loginRequest = LoginRequest.newBuilder()
                        .setOauthWechatPayload(OAuthWechatLoginPayload.newBuilder()
                                .setCode(body.getData().getCode()))
                        .setLoginType(LoginType.LOGIN_OAUTH_WECHAT)
                        .build();
                break;
            case OAUTH_MP_WECHAT:
                if (StringUtils.isEmpty(body.getData().getCode())) {
                    throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
                }

                loginRequest = LoginRequest.newBuilder()
                        .setOauthWechatPayload(OAuthWechatLoginPayload.newBuilder()
                                .setCode(body.getData().getCode()))
                        .setLoginType(LoginType.LOGIN_OAUTH_MP_WECHAT)
                        .build();
                break;
            case PASSWORD:
                if (StringUtils.isEmpty(body.getData().getUsername())
                        || StringUtils.isEmpty(body.getData().getPassword())) {
                    throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
                }

                loginRequest = LoginRequest.newBuilder()
                        .setPasswordPayload(PasswordLoginPayload.newBuilder()
                                .setUsername(body.getData().getUsername())
                                .setPassword(body.getData().getPassword()))
                        .setLoginType(LoginType.LOGIN_PASSWORD)
                        .build();
                break;
            case MOBILE:
                if (StringUtils.isEmpty(body.getData().getMobile())
                        || StringUtils.isEmpty(body.getData().getOtp())) {
                    throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
                }

                loginRequest = LoginRequest.newBuilder()
                        .setWebMobilePayload(WebMobileLoginPayload.newBuilder()
                                .setMobile(body.getData().getMobile())
                                .setOtp(body.getData().getOtp()))
                        .setLoginType(LoginType.LOGIN_WEB_MOBILE)
                        .build();
                break;
            case JWT_IOS:
                if (StringUtils.isEmpty(body.getData().getCode())) {
                    throw new KeepRealBusinessException(ErrorCode.REQUEST_INVALID_ARGUMENT);
                }

                loginRequest = LoginRequest.newBuilder()
                        .setJwtIsoLoginPayload(JWTISOLoginPayload.newBuilder()
                                .setIdentifyToken(body.getData().getCode()).build())
                        .setLoginType(LoginType.LOGIN_JWT_IOS)
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
     * @return {@link RefreshTokenResponse}.
     */
    @Override
    public ResponseEntity<RefreshTokenResponse> apiV1RefreshTokenPost(@Valid PostRefreshTokenRequest body) {
        LoginRequest loginRequest = LoginRequest.newBuilder()
                .setLoginType(LoginType.LOGIN_REFRESH_TOKEN)
                .setTokenRefreshPayload(TokenRefreshPayload.newBuilder()
                        .setRefreshToken(body.getRefreshToken())
                        .build())
                .build();

        com.keepreal.madagascar.baobob.LoginResponse loginResponse = this.loginService.login(loginRequest);

        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setData(this.buildBriefTokenInfo(loginResponse));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the set device token api.
     *
     * @param deviceTokenRequest {@link DeviceTokenRequest}.
     * @return {@link DummyResponse}.
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1DeviceTokensPost(@Valid DeviceTokenRequest deviceTokenRequest) {
        String userId = HttpContextUtils.getUserIdFromContext();
        userService.updateDeviceToken(userId, deviceTokenRequest.getDeviceToken(), deviceTokenRequest.getIsBind(), deviceTokenRequest.getDeviceType());
        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
        response.setData(this.userDTOFactory.valueOf(user, false));
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Implements the oss token get api.
     *
     * @return {@link OssTokenResponse}.
     */
    @Override
    public ResponseEntity<OssTokenResponse> apiV1OssTokenGet() {
        String userId = HttpContextUtils.getUserIdFromContext();

        final AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysEndpoint(this.ossClientConfiguration.getStsEndpoint());
        request.setSysMethod(MethodType.POST);
        request.setRoleArn(this.ossClientConfiguration.getRoleArn());
        request.setRoleSessionName("id_" + userId);
        request.setDurationSeconds(3600L);
        try {
            AssumeRoleResponse roleResponse = this.acsClient.getAcsResponse(request);

            OssTokenResponse response = new OssTokenResponse();
            response.setData(this.buildOssTokenDTO(roleResponse));
            response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
            response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ClientException e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }
    }

    /**
     * initialize and verify wechat official accounts server
     *
     * @param signature wechat server signature
     * @param timestamp wechat server timestamp
     * @param nonce     wechat server random number
     * @param echostr   wechat server random string
     * @return check successful return echostr
     */
    @GetMapping("/api/v1/events/wechatMp/callback")
    public String verifyWechatServer(String signature, String timestamp, String nonce, String echostr) {
        if (Objects.nonNull(signature) && Objects.nonNull(timestamp) && Objects.nonNull(nonce) && Objects.nonNull(echostr)) {
            CheckSignatureRequest request = CheckSignatureRequest.newBuilder().setSignature(signature).setTimestamp(timestamp).setNonce(nonce).build();
            if (this.loginService.checkSignature(request)) {
                return echostr;
            }
        }
        return null;
    }

    /**
     * receive wechat server event
     */
    @PostMapping("/api/v1/events/wechatMp/callback")
    public String receiveWechatServerEventPush(HttpServletRequest httpServletRequest) throws Exception {
        String requestXml = IOUtils.toString(httpServletRequest.getInputStream(), Charset.forName(httpServletRequest.getCharacterEncoding()));
        Map<String, String> request = WXPayUtil.xmlToMap(requestXml);
        String fromUserName = request.get("FromUserName");
        String msgType = request.get("MsgType");
        String event = request.get("Event");
        String eventKey = request.get("EventKey");
        if (Objects.nonNull(fromUserName) && Objects.nonNull(msgType) && Objects.nonNull(event) && Objects.nonNull(eventKey)) {
            if ("event".equals(msgType)) {
                this.loginService.handleEvent(fromUserName, event, eventKey);
            }
        }
        return "success";
    }

    /**
     * Generates web login qrcode.
     *
     * @return {@link QrTicketResponse}.
     */
    @Override
    @CrossOrigin
    public ResponseEntity<QrTicketResponse> apiV1LoginGeneratePost() {
        GenerateQrcodeResponse generateQrcodeResponse = loginService.generateQrcode();
        QrTicketResponse response = new QrTicketResponse();
        QrTicketDTO qrTicketDTO = new QrTicketDTO();

        qrTicketDTO.setTicket(generateQrcodeResponse.getTicket());
        qrTicketDTO.setExpirationInSec(generateQrcodeResponse.getExpirationInSec());
        qrTicketDTO.setSceneId(generateQrcodeResponse.getSceneId());

        response.setData(qrTicketDTO);
        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Retrieves web qrcode login information.
     *
     * @param sceneId (required) unique scene id.
     * @return {@link LoginResponse}.
     */
    @Override
    @CrossOrigin
    public ResponseEntity<LoginResponse> apiV1LoginPollingGet(String sceneId) {
        com.keepreal.madagascar.baobob.LoginResponse loginResponse = this.loginService.checkWechatMpAccountLogin(sceneId);

        LoginResponse response = new LoginResponse();
        if (!StringUtils.isEmpty(loginResponse.getUserId())) {
            UserMessage user = this.userService.retrieveUserById(loginResponse.getUserId());
            response.setData(this.buildTokenInfo(loginResponse, user));
        }

        response.setRtn(ErrorCode.REQUEST_SUCC.getNumber());
        response.setMsg(ErrorCode.REQUEST_SUCC.getValueDescriptor().getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 向指定手机号发送验证码
     *
     * @param postOTPRequest  (required) {@link PostOTPRequest}
     * @return {@link DummyResponse}
     */
    @Override
    public ResponseEntity<DummyResponse> apiV1MobileOtpPost(@Valid PostOTPRequest postOTPRequest,  @Valid Boolean login) {

        if (!login) {
            this.userService.checkUserMobileIsExisted(postOTPRequest.getMobile());
        }
        this.userService.sendOtpToMobile(postOTPRequest.getMobile());

        DummyResponse response = new DummyResponse();
        DummyResponseUtils.setRtnAndMessage(response, ErrorCode.REQUEST_SUCC);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Builds the {@link OssTokenDTO}.
     *
     * @param response {@link AssumeRoleResponse}.
     * @return {@link OssTokenDTO}.
     */
    private OssTokenDTO buildOssTokenDTO(AssumeRoleResponse response) {
        OssTokenDTO ossTokenDTO = new OssTokenDTO();
        ossTokenDTO.setAccessKey(response.getCredentials().getAccessKeyId());
        ossTokenDTO.setAccessSecret(response.getCredentials().getAccessKeySecret());
        ossTokenDTO.setSecurityToken(response.getCredentials().getSecurityToken());
        ZonedDateTime datetime = ZonedDateTime.parse(response.getCredentials().getExpiration());
        ossTokenDTO.setExpiration(datetime.toInstant().getEpochSecond());

        return ossTokenDTO;
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
        loginTokenInfo.setUser(this.userDTOFactory.valueOf(userMessage, false));
        loginTokenInfo.setOpenId(loginResponse.getOpenId());
        return loginTokenInfo;
    }

    /**
     * Builds the {@link BriefTokenInfo} from a {@link com.keepreal.madagascar.baobob.LoginResponse}.
     *
     * @param loginResponse {@link com.keepreal.madagascar.baobob.LoginResponse}.
     * @return {@link BriefTokenInfo}.
     */
    private BriefTokenInfo buildBriefTokenInfo(com.keepreal.madagascar.baobob.LoginResponse loginResponse) {
        BriefTokenInfo briefTokenInfo = new BriefTokenInfo();
        briefTokenInfo.setToken(loginResponse.getToken());
        briefTokenInfo.setRefreshToken(loginResponse.getRefreshToken());
        return briefTokenInfo;
    }
}
