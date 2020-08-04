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
import com.keepreal.madagascar.baobob.util.SingletonTokenUtils;
import com.keepreal.madagascar.common.EmptyMessage;
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
    private final static String GET_WECHAT_USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info";
    private final Gson gson;
    private final OauthWechatLoginConfiguration oauthWechatLoginConfiguration;
    private final RedissonClient redissonClient;
    private final GrpcResponseUtils grpcResponseUtils;

    /**
     * Constructs the mp wechat service.
     *
     * @param oauthWechatLoginConfiguration
     * @param redissonClient
     * @param userService
     */
    public MpWechatService(@Qualifier("wechatMpConfiguration") OauthWechatLoginConfiguration oauthWechatLoginConfiguration,
                           RedissonClient redissonClient,
                           UserService userService) {
        this.oauthWechatLoginConfiguration = oauthWechatLoginConfiguration;
        this.gson = new Gson();
        this.redissonClient = redissonClient;
        this.grpcResponseUtils = new GrpcResponseUtils();
    }

    /**
     * Retrieves the qrcode ticket.
     *
     * @return {@link GenerateQrcodeResponse}.
     */
    public Mono<GenerateQrcodeResponse> getTempQrcode() {
        String accessToken = this.getAccessToken();

        JSONObject requestBody = new JSONObject();
        JSONObject actionInfoBody = new JSONObject();
        JSONObject sceneBody = new JSONObject();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        sceneBody.put("scene_str", uuid);
        actionInfoBody.put("scene", sceneBody);
        requestBody.put("expire_seconds", this.oauthWechatLoginConfiguration.getExpirationInSec());
        requestBody.put("action_name", "QR_STR_SCENE");
        requestBody.put("action_info", actionInfoBody);

        String getQrcodeUrl = String.format(RETRIEVE_TEMP_QRCODE_URL, accessToken);
        return WebClient.create(getQrcodeUrl)
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
                .switchIfEmpty(Mono.error(new KeepRealBusinessException(ErrorCode.REQUEST_GRPC_LOGIN_INVALID)));
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
        String localSignature = byte2Str(digest);

        CheckSignatureResponse.Builder responseBuilder = CheckSignatureResponse.newBuilder();
        if (localSignature.equals(request.getSignature())) {
            responseBuilder.setStatus(this.grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_SUCC));
        } else {
            responseBuilder.setStatus(this.grpcResponseUtils.buildCommonStatus(ErrorCode.REQUEST_UNEXPECTED_ERROR));
        }
        return responseBuilder.build();
    }

    public EmptyMessage handleEvent(HandleEventRequest request) {
        String opedId = request.getOpedId();
        String event = request.getEvent();
        String eventKey = request.getEventKey();
        switch (event) {
            case "subscribe":
                executeSubscribeEvent(opedId, eventKey);
                break;
            case "SCAN":
                executeScanEvent(opedId, eventKey);
                break;
            default:
                log.info("unhandled event is {}", event);
        }
        return EmptyMessage.newBuilder().build();
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

    private String byte2HexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
                'b', 'c', 'd', 'e', 'f'};
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

    private String getAccessToken() {
        String accessToken = SingletonTokenUtils.getInstance().getAccessToken(oauthWechatLoginConfiguration.getAppId(),
                oauthWechatLoginConfiguration.getAppSecret());
        if (Objects.isNull(accessToken)) {
            return getAccessToken();
        }
        return accessToken;
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

}
