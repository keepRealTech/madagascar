package com.keepreal.madagascar.angonoka.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
    private final List<String> codeList = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z");
    private final List<Integer> codePeriods = Stream.of(0, 1, 2, 3).collect(Collectors.toList());

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
    public WeiboProfileResponse.Builder retrieveWeiboProfileByName(String name) {

        WeiboProfileResponse.Builder builder = WeiboProfileResponse.newBuilder();

        ResponseEntity<HashMap> responseEntity = this.restTemplate.getForEntity(
                String.format(WeiboApi.SHOW_USER_URL_BY_NAME, weiboBusinessConfig.getAccessToken(), name),
                HashMap.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR));
        }

        JsonObject body = this.gson.toJsonTree(responseEntity.getBody()).getAsJsonObject();

        if (Objects.isNull(body)) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR));
        }

        long totalNumber = body.get("total_number").getAsLong();
        if (totalNumber == 0) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_ACCOUNT_NOT_FOUND));
        }

        JsonArray jsonArray = body.get("users").getAsJsonArray();
        JsonObject jsonUser = jsonArray.get(0).getAsJsonObject();
        return builder.setStatus(CommonStatusUtils.getSuccStatus())
                .setWeiboMessage(WeiboProfileMessage.newBuilder()
                        .setId(jsonUser.get("idstr").getAsString())
                        .setName(jsonUser.get("screen_name").getAsString())
                        .setFollowerCount(jsonUser.get("followers_count").getAsLong())
                        .setAvatarUrl(jsonUser.get("avatar_hd").getAsString())
                        .build());
    }

    /**
     * 根据uid获取微博信息
     *
     * @param uid 微博唯一uid
     * @return {@link WeiboProfileResponse}
     */
    @Deprecated
    public WeiboProfileResponse.Builder retrieveWeiboProfileByUid(String uid) {

        WeiboProfileResponse.Builder builder = WeiboProfileResponse.newBuilder();

        ResponseEntity<HashMap> responseEntity = this.restTemplate.getForEntity(
                String.format(WeiboApi.SHOW_USER_URL_BY_UID, weiboBusinessConfig.getAccessToken(), uid),
                HashMap.class);

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR));
        }

        JsonObject body = this.gson.toJsonTree(responseEntity.getBody()).getAsJsonObject();

        if (Objects.isNull(body)) {
            return builder.setStatus(CommonStatusUtils.buildCommonStatus(ErrorCode.REQUEST_WEIBO_RPC_ERROR));
        }

        return builder.setStatus(CommonStatusUtils.getSuccStatus())
                .setWeiboMessage(WeiboProfileMessage.newBuilder()
                        .setId(body.get("idstr").getAsString())
                        .setName(body.get("screen_name").getAsString())
                        .setFollowerCount(body.get("followers_count").getAsLong())
                        .setAvatarUrl(body.get("avatar_hd").getAsString())
                        .build());
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
     * @param platformId platform id
     * @param type platform type
     */
    public void createSuperFollowSubscription(String hostId, String openId, String platformId, int type) {
        SuperFollowSubscription subscription = SuperFollowSubscription.builder()
                .id(String.valueOf(this.idGenerator.nextId()))
                .openId(openId)
                .hostId(hostId)
                .platformId(platformId)
                .type(type)
                .build();
        this.superFollowSubscriptionRepository.save(subscription);
    }

    /**
     * 创建超级关注消息
     *
     * @param id 主键id
     */
    public SuperFollow retrieveSuperFollowById(String id) {
        return this.superFollowRepository.findTopByIdAndState(id, FollowState.ENABLE_VALUE);
    }

    /**
     * 获取超级关注订阅消息
     *
     * @param hostId host id
     * @param openId open id
     * @param platformId platform id
     * @param type platform type
     */
    public SuperFollowSubscription retrieveSuperFollowSubscription(String hostId, String openId, String platformId, int type) {
        return this.superFollowSubscriptionRepository.findTopByOpenIdAndHostIdAndPlatformIdAndType(openId, hostId, platformId, type);
    }

    /**
     * 创建超级关注
     *
     * @param hostId host id
     * @param islandId island id
     * @param weiboUid weibo uid
     * @param followType follow type
     */
    public SuperFollow createSuperFollow(String hostId, String islandId, String weiboUid, FollowType followType) {
        try (AutoRedisLock ignored = new AutoRedisLock(redissonClient, "try-create-super-follow")) {
            SuperFollow superFollow = SuperFollow.builder()
                    .id(String.valueOf(this.idGenerator.nextId()))
                    .platformId(weiboUid)
                    .hostId(hostId)
                    .islandId(islandId)
                    .code(this.generateCodeV2())
                    .type(followType.getNumber())
                    .build();
            return this.superFollowRepository.save(superFollow);
        }
    }

    /**
     * retrieve super follow
     *
     * @param hostId host id
     * @param followType {@link FollowType}
     * @return {@link SuperFollow}
     */
    public SuperFollow retrieveSuperFollowByHostId(String hostId, FollowType followType) {
        return this.superFollowRepository.findTopByHostIdAndStateAndType(hostId, FollowState.ENABLE_VALUE, followType.getNumber());
    }

    /**
     * update super follow
     *
     * @param superFollow {@link SuperFollow}
     * @return {@link SuperFollow}
     */
    public SuperFollow updateSuperFollow(SuperFollow superFollow) {
        return this.superFollowRepository.save(superFollow);
    }

    /**
     * 顺序生成code
     *
     * @param startCode start code
     * @return code
     */
    @Deprecated
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
     * 随机生成code
     *
     * @return code
     */
    private String generateCodeV2() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(this.codeList.get((int) (Math.floor(Math.random() * 36))));
        }
        SuperFollow superFollow = this.retrieveSuperFollowMessageByCode(code.toString());
        if (Objects.isNull(superFollow)) {
            return code.toString();
        } else {
            return this.generateCodeV2();
        }
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
