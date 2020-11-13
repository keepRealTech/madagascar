package com.keepreal.madagascar.angonoka.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.keepreal.madagascar.angonoka.FollowType;
import com.keepreal.madagascar.angonoka.WeiboProfileMessage;
import com.keepreal.madagascar.angonoka.WeiboProfileResponse;
import com.keepreal.madagascar.angonoka.api.WeiboApi;
import com.keepreal.madagascar.angonoka.config.WeiboBusinessConfig;
import com.keepreal.madagascar.angonoka.dao.SuperFollowRepository;
import com.keepreal.madagascar.angonoka.dao.SuperFollowSubscriptionRepository;
import com.keepreal.madagascar.angonoka.model.SuperFollow;
import com.keepreal.madagascar.angonoka.model.SuperFollowSubscription;
import com.keepreal.madagascar.angonoka.util.CommonStatusUtils;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.common.enums.SuperFollowState;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Represents the follow GRpc controller.
 */
@Service
public class FollowService {
    private final RestTemplate restTemplate;
    private final WeiboBusinessConfig weiboBusinessConfig;
    private final Gson gson;
    private final SimpleDateFormat dateFormat;
    private final SuperFollowRepository superFollowRepository;
    private final SuperFollowSubscriptionRepository superFollowSubscriptionRepository;
    private final MpWechatService mpWechatService;

    /**
     * Constructs the follow service
     *
     * @param restTemplate {@link RestTemplate}
     * @param weiboBusinessConfig {@link WeiboBusinessConfig}
     * @param superFollowRepository {@link SuperFollowRepository}
     * @param superFollowSubscriptionRepository {@link SuperFollowSubscriptionRepository}
     * @param mpWechatService {@link MpWechatService}
     */
    public FollowService(RestTemplate restTemplate,
                         WeiboBusinessConfig weiboBusinessConfig,
                         SuperFollowRepository superFollowRepository,
                         SuperFollowSubscriptionRepository superFollowSubscriptionRepository,
                         MpWechatService mpWechatService) {
        this.restTemplate = restTemplate;
        this.weiboBusinessConfig = weiboBusinessConfig;
        this.superFollowRepository = superFollowRepository;
        this.superFollowSubscriptionRepository = superFollowSubscriptionRepository;
        this.mpWechatService = mpWechatService;
        this.gson = new Gson();
        this.dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("ENGLISH", "CHINA"));
    }

    /**
     * 根据昵称获取微博信息
     *
     * @param name 昵称
     * @return {@link WeiboProfileResponse}
     */
    public WeiboProfileResponse retrieveWeiboProfileByName(String name) {

        WeiboProfileResponse.Builder builder = WeiboProfileResponse.newBuilder();

        ResponseEntity<HashMap> responseEntity = this.restTemplate.getForEntity(
                String.format(WeiboApi.SHOW_USER_URL, weiboBusinessConfig.getAccessToken(), name),
                HashMap.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        JsonObject body = this.gson.toJsonTree(responseEntity.getBody()).getAsJsonObject();

        if (Objects.isNull(body)) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR)).build();
        }

        long totalNumber = body.get("total_number").getAsLong();
        if (totalNumber == 0) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_ACCOUNT_NOT_FOUND)).build();
        }

        JsonArray jsonArray = body.get("users").getAsJsonArray();
        JsonObject jsonUser = jsonArray.get(0).getAsJsonObject();
        return builder.setStatus(CommonStatusUtils.getSuccStatus())
                .setWeiboMessage(WeiboProfileMessage.newBuilder()
                        .setId(jsonUser.get("idstr").getAsString())
                        .setName(jsonUser.get("screen_name").getAsString())
                        .setFollowerCount(jsonUser.get("followers_count").getAsLong())
                        .setAvatarUrl(jsonUser.get("avatar_hd").getAsString())
                        .build())
                .build();
    }

    /**
     * 处理微博推送消息
     *
     * @param message 微博推送消息
     */
    public void handleWeiboSubscriptionMessage(String message) {
        JsonObject jsonObject = this.gson.fromJson(message, JsonObject.class);
        JsonObject text = jsonObject.get("text").getAsJsonObject();
        String event = text.get("event").getAsString();
        if (!"add".equals(event)) {
            return;
        }
        JsonObject status = text.get("status").getAsJsonObject();
        long createdAt = this.convertTime(status.get("created_at").getAsString());
        JsonObject user = status.get("user").getAsJsonObject();
        String platformId = user.get("id").getAsString();

        SuperFollow superFollow = this.superFollowRepository.findTopByPlatformIdAndTypeAndState(platformId,
                FollowType.FOLLOW_WEIBO_VALUE,
                SuperFollowState.ENABLED.getValue());
        if (superFollow.getLastPubTime() >= createdAt) {
            return;
        }

        List<SuperFollowSubscription> follower = this.superFollowSubscriptionRepository.findAllByHostIdAndTypeAndDeletedIsFalse(
                superFollow.getHostId(),
                FollowType.FOLLOW_WEIBO_VALUE);

        if (follower.isEmpty()) {
            return;
        }
        String screenName = user.get("screen_name").getAsString();
        String mid = status.get("mid").getAsString();
        String content = status.get("text").getAsString();
        String url = String.format(Templates.WEIBO_H5_URL, platformId, mid);

        List<String> openIds = follower.stream().map(SuperFollowSubscription::getOpenId).collect(Collectors.toList());

        this.mpWechatService.sendTemplateMessageByOpenIds(openIds, screenName, url, content);
    }

    /**
     * Converts the time.
     *
     * @param createdAt like "Thu Nov 12 10:41:09 +0800 2020"
     * @return millisecond
     */
    @SneakyThrows
    private long convertTime(String createdAt) {
        return this.dateFormat.parse(createdAt).getTime();
    }

}
