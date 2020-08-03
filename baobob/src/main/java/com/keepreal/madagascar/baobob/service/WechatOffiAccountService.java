package com.keepreal.madagascar.baobob.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.keepreal.madagascar.baobob.CheckOffiAccountLoginRequest;
import com.keepreal.madagascar.baobob.CheckSignatureRequest;
import com.keepreal.madagascar.baobob.CheckSignatureResponse;
import com.keepreal.madagascar.baobob.GenerateQrcodeResponse;
import com.keepreal.madagascar.baobob.HandleEventRequest;
import com.keepreal.madagascar.baobob.LoginResponse;
import com.keepreal.madagascar.baobob.NullResponse;
import com.keepreal.madagascar.baobob.config.wechat.WechatOffiAccountConfiguration;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatLoginInfo;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatOffiAccountToken;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatUserInfo;
import com.keepreal.madagascar.baobob.tokenGranter.LocalTokenGranter;
import com.keepreal.madagascar.baobob.util.CommonStatusUtils;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.CommonStatus;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WechatOffiAccountService {

    private final static String GET_WECHAT_OFFICIAL_ACCOUNT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private final static String RETRIEVE_TEMP_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create";
    private final static String GET_WECHAT_USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info";
    private final Gson gson;
    private final WechatOffiAccountConfiguration wechatOffiAccountConfig;
    private final RestTemplate restTemplate;
    private final RedissonClient redissonClient;
    private final UserService userService;
    private final ImageService imageService;
    private final AuthorizationServerEndpointsConfiguration endpoints;
    private final GrpcResponseUtils grpcResponseUtils;

    public WechatOffiAccountService(WechatOffiAccountConfiguration wechatOffiAccountConfig,
                                    RedissonClient redissonClient,
                                    UserService userService,
                                    ImageService imageService,
                                    AuthorizationServerEndpointsConfiguration endpoints,
                                    RestTemplate restTemplate) {
        this.wechatOffiAccountConfig = wechatOffiAccountConfig;
        this.gson = new Gson();
        this.redissonClient = redissonClient;
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.imageService = imageService;
        this.endpoints = endpoints;
        this.grpcResponseUtils = new GrpcResponseUtils();
    }

    @Cacheable(value = "accessToken")
    public String getAccessToken() {
        return getAccessTokenLoop();
    }

    @CachePut(value = "accessToken")
    public String getAccessTokenLoop() {
        String appId = wechatOffiAccountConfig.getAppId();
        String appSecret = wechatOffiAccountConfig.getAppSecret();
        String getTokenUrl = String.format(GET_WECHAT_OFFICIAL_ACCOUNT_ACCESS_TOKEN_URL +
                        "?grant_type=client_credential&appid=%s&secret=%s",
                         appId,
                         appSecret);
        WechatOffiAccountToken token = this.restTemplate.getForObject(getTokenUrl, WechatOffiAccountToken.class);
        if (Objects.isNull(token)){
            return getAccessTokenLoop();
        }
        log.info("token is {}", token.getAccess_token());
        return token.getAccess_token();
    }

    @Scheduled(cron = "0 0 0/1 * * ? ")
    public void updateAccessToken(){
        log.info("更新公众号access_token");
        getAccessTokenLoop();
    }

    public GenerateQrcodeResponse getTempQrcode() {
        String accessToken = this.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        JSONObject actionInfoBody = new JSONObject();
        JSONObject sceneBody = new JSONObject();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        sceneBody.put("scene_str", uuid);
        actionInfoBody.put("scene", sceneBody);
        requestBody.put("expire_seconds", wechatOffiAccountConfig.getExpirationInSec());
        requestBody.put("action_name", "QR_STR_SCENE");
        requestBody.put("action_info", actionInfoBody);
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        String getQrcodeUrl = String.format(RETRIEVE_TEMP_QRCODE_URL + "?access_token=%s", accessToken);
        ResponseEntity<String> response = this.restTemplate.postForEntity(getQrcodeUrl, request, String.class);
        if (response.getStatusCodeValue() != 200){
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        HashMap responseBody = gson.fromJson(response.getBody(), HashMap.class);
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
        String token = wechatOffiAccountConfig.getServerToken();
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
            responseBuilder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
        }else {
            responseBuilder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_UNEXPECTED_ERROR));
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

    public NullResponse handleEvent(HandleEventRequest request) {
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
        return NullResponse.newBuilder().build();
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
        String accessToken = getAccessToken();
        String getUserInfoUrl = String.format(GET_WECHAT_USER_INFO_URL + "?access_token=%s&openid=%s&lang=zh_CN", accessToken, openId);
        ResponseEntity<String> response = restTemplate.getForEntity(getUserInfoUrl, String.class);
        String body = response.getBody();
        HashMap hashMap = this.gson.fromJson(body, HashMap.class);
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
