package com.keepreal.madagascar.hawksbill.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.keepreal.madagascar.hawksbill.api.MpWechatApi;
import com.keepreal.madagascar.hawksbill.config.MpWechatConfiguration;
import com.keepreal.madagascar.hawksbill.util.AutoRedisLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MpWechatService {
    private final MpWechatConfiguration mpWechatConfiguration;
    private final RestTemplate restTemplate;
    private final RedissonClient redissonClient;
    private final Gson gson;

    public MpWechatService(MpWechatConfiguration mpWechatConfiguration,
                           RestTemplate restTemplate,
                           RedissonClient redissonClient,
                           Gson gson) {
        this.mpWechatConfiguration = mpWechatConfiguration;
        this.restTemplate = restTemplate;
        this.redissonClient = redissonClient;
        this.gson = new Gson();
    }

    public void sendTemplateMessageByOpenId(String openId, String name, String url, String text) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("touser", openId);
        requestBody.addProperty("template_id", this.mpWechatConfiguration.getTemplateId());
        requestBody.addProperty("url", url);
        JsonObject data = new JsonObject();
        JsonObject first = new JsonObject();
        first.addProperty("value", "您关注的创作者有新的微博");
        JsonObject creatorName = new JsonObject();
        creatorName.addProperty("value", name);
        JsonObject content = new JsonObject();
        if (text.length() > 10) {
            text = text.substring(0, 9) + "...";
        }
        content.addProperty("value", text);
        data.add("first",first);
        data.add("keyword1",content);
        data.add("keyword2",creatorName);
        requestBody.add("data", data);

        String accessToken = this.getAccessToken();

        ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(String.format(MpWechatApi.POST_TEMPLATE_MESSAGE, accessToken),
                requestBody,
                String.class);
        if (responseEntity.getStatusCodeValue() != 200) {
            log.error("send send Template Message Error!");
        }
    }

    /**
     * 获取永久二维码
     *
     * @return 二维码ticket
     */
    public String retrievePermanentQRCode() {
        String accessToken = this.getAccessToken();
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("action_name", "QR_LIMIT_STR_SCENE");
        JsonObject actionInfo = new JsonObject();
        JsonObject scene = new JsonObject();
        scene.addProperty("scene_str", "superFollow");
        actionInfo.add("scene", scene);
        requestBody.add("action_info", actionInfo);
        ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(String.format(MpWechatApi.POST_PERMANENT_QRCODE, accessToken),
                requestBody.toString(),
                String.class);
        JsonObject responseBody = this.gson.fromJson(responseEntity.getBody(), JsonObject.class);
        return responseBody.get("ticket").getAsString();
    }

    /**
     * 获得公众号 access token
     *
     * @return access token
     */
    public String getAccessToken() {
        RBucket<String> accessToken = this.redissonClient.getBucket("wechat-mp-access-token");
        if (accessToken.isExists()) {
            return accessToken.get();
        }
        return this.retrieveNewAccessToken(accessToken);
    }

    /**
     * 更新公众号 access token
     *
     * @param bucket {@link RBucket}
     * @return access token
     */
    private String retrieveNewAccessToken(RBucket<String> bucket) {
        try (AutoRedisLock ignored = new AutoRedisLock(this.redissonClient, "try-get-mp-access-token")) {
            String getTokenUrl = String.format(MpWechatApi.GET_WECHAT_OFFICIAL_ACCOUNT_ACCESS_TOKEN_URL,
                    this.mpWechatConfiguration.getAppId(), this.mpWechatConfiguration.getAppSecret());

            ResponseEntity<String> responseEntity = this.restTemplate.getForEntity(getTokenUrl, String.class);
            JsonObject responseBody = this.gson.fromJson(responseEntity.getBody(), JsonObject.class);
            String accessToken = responseBody.get("access_token").getAsString();
            long expiresInSec = responseBody.get("expires_in").getAsLong();
            bucket.trySet(accessToken, expiresInSec - 200L, TimeUnit.SECONDS);
            return accessToken;
        }
    }

}
