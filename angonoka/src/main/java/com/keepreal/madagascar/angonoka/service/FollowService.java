package com.keepreal.madagascar.angonoka.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.keepreal.madagascar.angonoka.FollowState;
import com.keepreal.madagascar.angonoka.FollowType;
import com.keepreal.madagascar.angonoka.RetrieveAllSuperFollowResponse;
import com.keepreal.madagascar.angonoka.SuperFollowMessage;
import com.keepreal.madagascar.angonoka.WeiboProfileMessage;
import com.keepreal.madagascar.angonoka.WeiboProfileResponse;
import com.keepreal.madagascar.angonoka.api.WeiboApi;
import com.keepreal.madagascar.angonoka.config.WeiboBusinessConfig;
import com.keepreal.madagascar.angonoka.dao.SuperFollowRepository;
import com.keepreal.madagascar.angonoka.dao.SuperFollowSubscriptionRepository;
import com.keepreal.madagascar.angonoka.model.SuperFollow;
import com.keepreal.madagascar.angonoka.model.SuperFollowSubscription;
import com.keepreal.madagascar.angonoka.util.AutoRedisLock;
import com.keepreal.madagascar.angonoka.util.CommonStatusUtils;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.common.enums.SuperFollowState;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Represents the follow GRpc controller.
 */
@Service
@Slf4j
public class FollowService {
    private final RestTemplate restTemplate;
    private final WeiboBusinessConfig weiboBusinessConfig;
    private final Gson gson;
    private final SimpleDateFormat dateFormat;
    private final SuperFollowRepository superFollowRepository;
    private final SuperFollowSubscriptionRepository superFollowSubscriptionRepository;
    private final MpWechatService mpWechatService;
    private final LongIdGenerator idGenerator;
    private final RedissonClient redissonClient;

    /**
     * Constructs the follow service
     *
     * @param restTemplate {@link RestTemplate}
     * @param weiboBusinessConfig {@link WeiboBusinessConfig}
     * @param superFollowRepository {@link SuperFollowRepository}
     * @param superFollowSubscriptionRepository {@link SuperFollowSubscriptionRepository}
     * @param mpWechatService {@link MpWechatService}
     * @param idGenerator {@link LongIdGenerator}
     * @param redissonClient {@link RedissonClient}
     */
    public FollowService(RestTemplate restTemplate,
                         WeiboBusinessConfig weiboBusinessConfig,
                         SuperFollowRepository superFollowRepository,
                         SuperFollowSubscriptionRepository superFollowSubscriptionRepository,
                         MpWechatService mpWechatService,
                         LongIdGenerator idGenerator,
                         RedissonClient redissonClient) {
        this.restTemplate = restTemplate;
        this.weiboBusinessConfig = weiboBusinessConfig;
        this.superFollowRepository = superFollowRepository;
        this.superFollowSubscriptionRepository = superFollowSubscriptionRepository;
        this.mpWechatService = mpWechatService;
        this.idGenerator = idGenerator;
        this.redissonClient = redissonClient;
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
        long sinceId = jsonObject.get("id").getAsLong();
        try (AutoRedisLock ignored = new AutoRedisLock(redissonClient, "try-set-weibo-sinceId")) {
            RBucket<Long> bucket = redissonClient.getBucket("weibo-sinceId");
            bucket.set(sinceId);
        }
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

        if (Objects.isNull(superFollow)) {
            log.error("super follow not found platform id is {}", platformId);
            return;
        }

        if (superFollow.getLastPubTime() >= createdAt) {
            return;
        }

        List<SuperFollowSubscription> follower = this.superFollowSubscriptionRepository.findAllByHostIdAndTypeAndDeletedIsFalse(
                superFollow.getHostId(),
                FollowType.FOLLOW_WEIBO_VALUE);

        if (follower.isEmpty()) {
            return;
        }

        superFollow.setLastPubTime(createdAt);
        this.updateSuperFollow(superFollow);

        String screenName = user.get("screen_name").getAsString();
        String mid = status.get("mid").getAsString();
        String content = status.get("text").getAsString();
        String url = String.format(Templates.WEIBO_H5_URL, platformId, mid);

        List<String> openIds = follower.stream().map(SuperFollowSubscription::getOpenId).collect(Collectors.toList());

        this.mpWechatService.sendTemplateMessageByOpenIds(openIds, screenName, url, content);
    }

    /**
     * 根据暗号获取超级关注信息
     *
     * @param code 暗号
     * @return {@link SuperFollow}
     */
    public SuperFollow retrieveSuperFollowMessageByCode(String code) {
        return this.superFollowRepository.findTopByCodeAndState(code, FollowState.ENABLE_VALUE);
    }

    /**
     * 根据host id 获取所有超级关注信息
     *
     * @param hostId hostId
     * @return {@link SuperFollow}
     */
    public List<SuperFollow> retrieveAllSuperFollowMessageByCode(String hostId) {
        return this.superFollowRepository.findAllByHostIdAndState(hostId, FollowState.ENABLE_VALUE);
    }

    public RetrieveAllSuperFollowResponse valueOf(List<SuperFollow> superFollowList) {
        RetrieveAllSuperFollowResponse.Builder builder = RetrieveAllSuperFollowResponse.newBuilder();
        superFollowList.forEach(superFollow -> {
            switch (FollowType.forNumber(superFollow.getType())) {
                case FOLLOW_WEIBO:
                    builder.setWeibo(this.getSuperFollowMessage(superFollow));
                    break;
                case FOLLOW_TIKTOK:
                    builder.setTiktok(this.getSuperFollowMessage(superFollow));
                    break;
                case FOLLOW_BILIBILI:
                    builder.setBilibili(this.getSuperFollowMessage(superFollow));
                    break;
                default:
                    break;
            }
        });
        return builder.setStatus(CommonStatusUtils.getSuccStatus()).build();
    }

    public SuperFollowMessage getSuperFollowMessage(SuperFollow superFollow) {
        if (Objects.isNull(superFollow)) {
            return null;
        }

        // for now
        if (superFollow.getType() == FollowType.FOLLOW_BILIBILI_VALUE || superFollow.getType() == FollowType.FOLLOW_TIKTOK_VALUE) {
            return null;
        }

        return SuperFollowMessage.newBuilder()
                .setId(superFollow.getId())
                .setPlatformId(superFollow.getPlatformId())
                .setHostId(superFollow.getHostId())
                .setCreatedTime(superFollow.getCreatedTime())
                .setIslandId(superFollow.getIslandId())
                .setCode(superFollow.getCode())
                .setState(FollowState.forNumber(superFollow.getState()))
                .build();
    }

    /**
     * 创建超级关注订阅消息
     *
     * @param hostId host id
     * @param openId open id
     * @param superFollowId super follow id
     */
    public void createSuperFollowSubscription(String hostId, String openId, String superFollowId) {
        SuperFollow superFollow = this.superFollowRepository.findTopByIdAndState(superFollowId, FollowState.ENABLE_VALUE);
        SuperFollowSubscription subscription = SuperFollowSubscription.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .openId(openId)
                .hostId(hostId)
                .platformId(superFollow.getPlatformId())
                .type(superFollow.getType())
                .build();
        this.superFollowSubscriptionRepository.save(subscription);
    }

    /**
     * 创建超级关注
     *
     * @param hostId host id
     * @param islandId island id
     * @param weiboUid weibo uid
     * @param followType follow type
     */
    public void createSuperFollow(String hostId, String islandId, String weiboUid, FollowType followType) {
        String code = this.superFollowRepository.selectTopCodeByStateOrderByCreatedTime(FollowState.ENABLE_VALUE);
        SuperFollow superFollow = SuperFollow.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .platformId(weiboUid)
                .hostId(hostId)
                .islandId(islandId)
                .code(StringUtils.isEmpty(code) ? "0000" : this.generateCode(code))
                .type(followType.getNumber())
                .build();
        this.superFollowRepository.save(superFollow);
    }

    public SuperFollow retrieveSuperFollowByHostId(String hostId, FollowType followType) {
        return this.superFollowRepository.findTopByHostIdAndStateAndType(hostId, FollowState.ENABLE_VALUE, followType.getNumber());
    }

    public SuperFollow updateSuperFollow(SuperFollow superFollow) {
        return this.superFollowRepository.save(superFollow);
    }

    /**
     * 顺序生成code
     *
     * @param startCode start code
     * @return code
     */
    private String generateCode(String startCode) {
        char[] chars = startCode.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {

            if (chars[i] < 57) {
                chars[i] = (char)((int)chars[i] + 1);
                return String.valueOf(chars);
            }

            if (chars[i] == 57) {
                chars[i] = 'A';
                return String.valueOf(chars);
            }

            if (chars[i] < 90) {
                chars[i] = (char)((int)chars[i] + 1);
                return String.valueOf(chars);
            }

            chars[i] = '0';
        }
        log.error("超出限制！");
        return String.valueOf(10000);
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
