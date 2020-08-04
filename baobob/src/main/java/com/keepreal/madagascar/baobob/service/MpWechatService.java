package com.keepreal.madagascar.baobob.service;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.keepreal.madagascar.baobob.CheckOffiAccountLoginRequest;
import com.keepreal.madagascar.baobob.CheckSignatureRequest;
import com.keepreal.madagascar.baobob.CheckSignatureResponse;
import com.keepreal.madagascar.baobob.GenerateQrcodeResponse;
import com.keepreal.madagascar.baobob.HandleEventRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.config.wechat.OauthWechatLoginConfiguration;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatUserInfo;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.baobob.util.SingletonTokenUtils;
import com.keepreal.madagascar.common.EmptyMessage;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MpWechatService {

    private final static String GET_WECHAT_OFFICIAL_ACCOUNT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private final static String RETRIEVE_TEMP_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create";
    private final static String GET_WECHAT_USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info";
    private final Gson gson;
    private final OauthWechatLoginConfiguration oauthWechatLoginConfiguration;
    private final RedissonClient redissonClient;
    private final UserService userService;
    private final ImageService imageService;
    private final AuthorizationServerEndpointsConfiguration endpoints;
    private final GrpcResponseUtils grpcResponseUtils;

    public MpWechatService(@Qualifier("wechatMpConfiguration") OauthWechatLoginConfiguration oauthWechatLoginConfiguration,
                           RedissonClient redissonClient,
                           UserService userService,
                           ImageService imageService,
                           AuthorizationServerEndpointsConfiguration endpoints) {
        this.oauthWechatLoginConfiguration = oauthWechatLoginConfiguration;
        this.gson = new Gson();
        this.redissonClient = redissonClient;
        this.userService = userService;
        this.imageService = imageService;
        this.endpoints = endpoints;
        this.grpcResponseUtils = new GrpcResponseUtils();
    }

    private String getAccessToken() {
        String accessToken = SingletonTokenUtils.getInstance().getAccessToken(oauthWechatLoginConfiguration.getAppId(),
                oauthWechatLoginConfiguration.getAppSecret());
        if (Objects.isNull(accessToken)){
            return SingletonTokenUtils.getInstance().getAccessToken(oauthWechatLoginConfiguration.getAppId(),
                    oauthWechatLoginConfiguration.getAppSecret());
        }
        return accessToken;
    }

    public GenerateQrcodeResponse getTempQrcode() {
        String accessToken = this.getAccessToken();
        JSONObject requestBody = new JSONObject();
        JSONObject actionInfoBody = new JSONObject();
        JSONObject sceneBody = new JSONObject();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        sceneBody.put("scene_str", uuid);
        actionInfoBody.put("scene", sceneBody);
        requestBody.put("expire_seconds", oauthWechatLoginConfiguration.getExpirationInSec());
        requestBody.put("action_name", "QR_STR_SCENE");
        requestBody.put("action_info", actionInfoBody);

        String getQrcodeUrl = String.format(RETRIEVE_TEMP_QRCODE_URL + "?access_token=%s", accessToken);
        JSONObject responseBody = WebClient.create(getQrcodeUrl)
                .post()
                .header("Content-Type", "application/json")
                .syncBody(requestBody)
                .retrieve()
                .bodyToMono(JSONObject.class)
                .doOnError(error -> log.error(error.toString()))
                .block();

        if (Objects.isNull(responseBody)){
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        String ticket = String.valueOf(responseBody.get("ticket"));
        String[] expire_secondsTemp = String.valueOf(responseBody.get("expire_seconds")).split("\\.");
        Integer expire_seconds = Integer.parseInt(expire_secondsTemp[0]);

        return GenerateQrcodeResponse.newBuilder().setTicket(ticket)
                .setExpirationInSec(expire_seconds)
                .setSceneId(uuid)
                .setStatus(new GrpcResponseUtils().buildCommonStatus(ErrorCode.REQUEST_SUCC))
                .build();
    }

    @SneakyThrows
    public CheckSignatureResponse checkSignature(CheckSignatureRequest request) {
        String token = oauthWechatLoginConfiguration.getServerToken();
        String signature = request.getSignature();
        String timestamp = request.getTimestamp();
        String nonce = request.getNonce();

        String[] strs = new String[]{timestamp, nonce, token};
        Arrays.sort(strs);
        String content = StringUtils.collectionToDelimitedString(Arrays.asList(strs), "");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(content.getBytes());
        String localSignature = byte2Str(digest);

        CheckSignatureResponse.Builder responseBuilder = CheckSignatureResponse.newBuilder();
        if (localSignature.equals(signature)){
            responseBuilder.setStatus(grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
        }else {
            responseBuilder.setStatus(grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_UNEXPECTED_ERROR));
        }
        return responseBuilder.build();
    }

    private String byte2HexStr(byte mByte) {
        char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
                'b', 'c', 'd', 'e', 'f' };
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        return new String(tempArr);
    }

    private String byte2Str(byte[] byteArray) {
        StringBuilder strDigest = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            strDigest.append(byte2HexStr(byteArray[i]));
        }
        return strDigest.toString();
    }

    public EmptyMessage handleEvent(HandleEventRequest request) {
        String opedId = request.getOpedId();
        String event = request.getEvent();
        String eventKey = request.getEventKey();
        switch (event) {
            case "subscribe" :
                executeSubscribeEvent(opedId, eventKey);
                break;
            case "SCAN" :
                executeScanEvent(opedId, eventKey);
                break;
            default:
                log.info("unhandled event is {}", event);
        }
        return EmptyMessage.newBuilder().build();
    }

    private void executeSubscribeEvent(String openId, String eventKey) {
        String[] sceneStrs = StringUtils.delimitedListToStringArray(eventKey, "_");
        String sceneId = sceneStrs[0];
        WechatUserInfo wechatUserInfo = retrieveUserInfoFromWechatByOpenId(openId);
        RBucket<Object> bucket = this.redissonClient.getBucket(sceneId);
        bucket.trySet(wechatUserInfo, 1L, TimeUnit.MINUTES);
    }

    private void executeScanEvent(String openId, String eventKey) {
        WechatUserInfo wechatUserInfo = retrieveUserInfoFromWechatByOpenId(openId);
        RBucket<WechatUserInfo> bucket = this.redissonClient.getBucket(eventKey);
        bucket.trySet(wechatUserInfo, 1L, TimeUnit.MINUTES);
    }

    public Mono<LoginResponse> checkOffiAccountLogin(CheckOffiAccountLoginRequest request) {
        RBucket<WechatUserInfo> bucket = this.redissonClient.getBucket(request.getSceneId());
        WechatUserInfo wechatUserInfo = bucket.get();
        if (Objects.nonNull(wechatUserInfo)) {
            LocalTokenGranter tokenGranter = new LocalTokenGranter(
                    this.endpoints.getEndpointsConfigurer().getTokenServices(),
                    this.endpoints.getEndpointsConfigurer().getClientDetailsService(),
                    this.endpoints.getEndpointsConfigurer().getOAuth2RequestFactory());

            return this.retrieveOrCreateUserByUnionId(wechatUserInfo)
                    .map(usermessage -> tokenGranter.grant(usermessage, wechatUserInfo.getOpenId()))
                    .doOnError(error -> log.error(error.toString()))
                    .onErrorReturn(throwable -> throwable instanceof KeepRealBusinessException
                                    && ((KeepRealBusinessException) throwable).getErrorCode() == ErrorCode.REQUEST_GRPC_LOGIN_FROZEN,
                            this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN))
                    .onErrorReturn(this.grpcResponseUtils.buildInvalidLoginResponse(ErrorCode.REQUEST_GRPC_LOGIN_INVALID));
        }
        return Mono.just(LoginResponse.newBuilder().setStatus(grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC)).build());
    }

    private Mono<UserMessage> retrieveOrCreateUserByUnionId(WechatUserInfo wechatUserInfo) {
        assert !StringUtils.isEmpty(wechatUserInfo.getUnionId());
        return this.userService.retrieveUserByUnionIdMono(wechatUserInfo.getUnionId())
                .map(userMessage -> {
                    if (userMessage.getLocked()) {
                        throw new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_FROZEN);
                    }
                    return userMessage;
                })
                .switchIfEmpty(this.createNewUserFromRedisWechatInfo(wechatUserInfo));
    }

    /**
     * Creates a new user from wechat user info.
     *
     * @param wechatUserInfo {@link WechatUserInfo}.
     * @return {@link UserMessage}.
     */
    private Mono<UserMessage> createNewUserFromRedisWechatInfo(WechatUserInfo wechatUserInfo) {
        return this.migrateImage(wechatUserInfo)
                .flatMap(this.userService::createUserByWechatUserInfoMono);
    }



    private WechatUserInfo retrieveUserInfoFromWechatByOpenId(String openId) {
        String accessToken = this.getAccessToken();
        String getUserInfoUrl = String.format(GET_WECHAT_USER_INFO_URL + "?access_token=%s&openid=%s&lang=zh_CN", accessToken, openId);
        JSONObject responseBody = WebClient.create(getUserInfoUrl)
                .get()
                .retrieve()
                .bodyToMono(JSONObject.class)
                .doOnError(error -> log.error(error.toString()))
                .block();

        if (Objects.isNull(responseBody)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        HashMap hashMap = this.gson.fromJson(responseBody.toJSONString(), HashMap.class);
        return WechatUserInfo.builder()
                .name(String.valueOf(hashMap.getOrDefault("nickname", "")))
                .gender(this.convertGender(hashMap.getOrDefault("sex", 0)))
                .province(String.valueOf(hashMap.getOrDefault("province", "")))
                .city(String.valueOf(hashMap.getOrDefault("city", "")))
                .country(String.valueOf(hashMap.getOrDefault("country", "")))
                .portraitImageUri(String.valueOf(hashMap.getOrDefault("headimgurl", "")))
                .unionId(String.valueOf(hashMap.get("unionid")))
                .openId(openId)
                .build();
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
