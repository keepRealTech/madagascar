package com.keepreal.madagascar.baobob.service;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.keepreal.madagascar.baobob.CheckSignatureRequest;
import com.keepreal.madagascar.baobob.CheckSignatureResponse;
import com.keepreal.madagascar.baobob.GenerateQrcodeResponse;
import com.keepreal.madagascar.baobob.HandleEventRequest;
import com.keepreal.madagascar.baobob.config.wechat.OauthWechatLoginConfiguration;
import com.keepreal.madagascar.baobob.loginExecutor.model.WechatUserInfo;
import com.keepreal.madagascar.baobob.util.GrpcResponseUtils;
import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents the mp wechat service.
 */
@Slf4j
@Service
public class MpWechatService {

    private final static String RETRIEVE_TEMP_QRCODE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";
    private final static String GET_WECHAT_USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN";
    private static final String GET_WECHAT_OFFICIAL_ACCOUNT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private final Gson gson;
    private final OauthWechatLoginConfiguration oauthWechatLoginConfiguration;
    private final RedissonClient redissonClient;
    private final GrpcResponseUtils grpcResponseUtils;

    /**
     * Constructs the mp wechat service.
     *
     * @param oauthWechatLoginConfiguration {@link OauthWechatLoginConfiguration}.
     * @param redissonClient                {@link RedissonClient}.
     */
    public MpWechatService(@Qualifier("wechatMpConfiguration") OauthWechatLoginConfiguration oauthWechatLoginConfiguration,
                           RedissonClient redissonClient) {
        this.oauthWechatLoginConfiguration = oauthWechatLoginConfiguration;
        this.redissonClient = redissonClient;
        this.gson = new Gson();
        this.grpcResponseUtils = new GrpcResponseUtils();
    }

    /**
     * Retrieves the qrcode ticket.
     *
     * @return {@link GenerateQrcodeResponse}.
     */
    public Mono<GenerateQrcodeResponse> getTempQrcode() {
        JSONObject requestBody = new JSONObject();
        JSONObject actionInfoBody = new JSONObject();
        JSONObject sceneBody = new JSONObject();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        sceneBody.put("scene_str", uuid);
        actionInfoBody.put("scene", sceneBody);
        requestBody.put("expire_seconds", this.oauthWechatLoginConfiguration.getExpirationInSec());
        requestBody.put("action_name", "QR_STR_SCENE");
        requestBody.put("action_info", actionInfoBody);

        return this.getAccessToken()
                .map(token -> String.format(MpWechatService.RETRIEVE_TEMP_QRCODE_URL, token))
                .flatMap(url ->
                        WebClient.create(url)
                                .post()
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .syncBody(requestBody)
                                .retrieve()
                                .bodyToMono(String.class)
                                .map(response -> this.gson.fromJson(response, HashMap.class))
                                .map(hashMap -> {
                                    String ticket = String.valueOf(hashMap.get("ticket"));
                                    String[] expire_secondsTemp = String.valueOf(hashMap.get("expire_seconds")).split("\\.");
                                    int expire_seconds = Integer.parseInt(expire_secondsTemp[0]);

                                    return GenerateQrcodeResponse.newBuilder().setTicket(ticket)
                                            .setExpirationInSec(expire_seconds)
                                            .setSceneId(uuid)
                                            .setStatus(new GrpcResponseUtils().buildCommonStatus(ErrorCode.REQUEST_SUCC))
                                            .build();
                                })
                                .doOnError(error -> log.error(error.toString()))
                                .switchIfEmpty(Mono.error(new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID))));
    }

    /**
     * Checks the wehcat mp signature.
     *
     * @param request {@link CheckSignatureRequest}.
     * @return {@link CheckSignatureResponse}.
     */
    @SneakyThrows
    public CheckSignatureResponse checkSignature(CheckSignatureRequest request) {
        List<String> paramList = Arrays.asList(request.getTimestamp(), request.getNonce(),
                this.oauthWechatLoginConfiguration.getServerToken());
        paramList.sort(String::compareTo);
        String content = StringUtils.collectionToDelimitedString(paramList, "");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(content.getBytes());
        String localSignature = this.byte2Str(digest);

        CheckSignatureResponse.Builder responseBuilder = CheckSignatureResponse.newBuilder();
        if (localSignature.equals(request.getSignature())) {
            responseBuilder.setStatus(this.grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
        } else {
            responseBuilder.setStatus(this.grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_UNEXPECTED_ERROR));
        }
        return responseBuilder.build();
    }

    /**
     * Handles different MP wechat events.
     *
     * @param request {@link HandleEventRequest}.
     * @return {link Em}
     */
    public Mono<Void> handleEvent(HandleEventRequest request) {
        String opedId = request.getOpedId();
        String event = request.getEvent();
        String eventKey = request.getEventKey();
        switch (event) {
            case "subscribe":
                this.executeSubscribeEvent(opedId, eventKey);
                break;
            case "SCAN":
                this.executeScanEvent(opedId, eventKey);
                break;
            default:
                log.info("unhandled event is {}", event);
        }
        return Mono.empty();
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
     * Convert a byte to hex string.
     *
     * @param mByte Byte.
     * @return Hex string.
     */
    private String byte2HexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
                'b', 'c', 'd', 'e', 'f'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        return new String(tempArr);
    }

    /**
     * Converts a byte array into hex string.
     *
     * @param byteArray Byte array.
     * @return Hex string.
     */
    private String byte2Str(byte[] byteArray) {
        StringBuilder strDigest = new StringBuilder();
        for (byte b : byteArray) {
            strDigest.append(this.byte2HexStr(b));
        }
        return strDigest.toString();
    }

    /**
     * Gets the access token.
     *
     * @return Access token.
     */
    private Mono<String> getAccessToken() {
        RBucket<String> bucket = this.redissonClient.getBucket("wechat-mp-access-token");

        String accessToken = bucket.get();
        if (Objects.nonNull(accessToken)) {
            return Mono.just(accessToken);
        }
        return this.retrieveNewAccessToken(bucket);
    }

    /**
     * Handles the users first time subscribing.
     *
     * @param openId   User open id.
     * @param eventKey Event key.
     */
    private void executeSubscribeEvent(String openId, String eventKey) {
        String[] sceneStrs = StringUtils.delimitedListToStringArray(eventKey, "_");
        String sceneId = sceneStrs[0];
        WechatUserInfo wechatUserInfo = this.retrieveUserInfoFromWechatByOpenId(openId).block();
        RBucket<Object> bucket = this.redissonClient.getBucket(sceneId);
        bucket.trySet(wechatUserInfo, 1L, TimeUnit.MINUTES);
    }

    /**
     * Handles the users already subscribed.
     *
     * @param openId   User open id.
     * @param eventKey Event key.
     */
    private void executeScanEvent(String openId, String eventKey) {
        WechatUserInfo wechatUserInfo = this.retrieveUserInfoFromWechatByOpenId(openId).block();
        RBucket<WechatUserInfo> bucket = this.redissonClient.getBucket(eventKey);
        bucket.trySet(wechatUserInfo, 1L, TimeUnit.MINUTES);
    }

    /**
     * Retrieves wechat user info by open id.
     *
     * @param openId Open id.
     * @return {@link WechatUserInfo}.
     */
    @SuppressWarnings("unchecked")
    private Mono<WechatUserInfo> retrieveUserInfoFromWechatByOpenId(String openId) {
        return this.getAccessToken()
                .map(token -> String.format(MpWechatService.GET_WECHAT_USER_INFO_URL, token, openId))
                .flatMap(url -> WebClient.create(url)
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
                                        .openId(openId)
                                        .build()));
    }

    /**
     * Retrieves a new access token.
     *
     * @return Access token.
     */
    private Mono<String> retrieveNewAccessToken(RBucket<String> bucket) {
        String getTokenUrl = String.format(MpWechatService.GET_WECHAT_OFFICIAL_ACCOUNT_ACCESS_TOKEN_URL,
                this.oauthWechatLoginConfiguration.getAppId(), this.oauthWechatLoginConfiguration.getAppSecret());
        return WebClient.create(getTokenUrl)
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> this.gson.fromJson(response, HashMap.class))
                .flatMap(hashMap -> {
                    String accessToken = String.valueOf(hashMap.get("access_token"));
                    String expiresInSec = String.valueOf(hashMap.get("expires_in"));
                    bucket.trySet(accessToken, System.currentTimeMillis() + (Long.parseLong(expiresInSec) - 200) * 1000L, TimeUnit.MILLISECONDS);
                    return Mono.just(accessToken);
                });
    }

}
