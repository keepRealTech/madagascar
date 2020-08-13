package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.DeviceType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.RepostMessage;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.FeedRepostMessage;
import com.keepreal.madagascar.fossa.IslandRepostMessage;
import com.keepreal.madagascar.fossa.common.RepostType;
import com.keepreal.madagascar.fossa.config.GeneralConfiguration;
import com.keepreal.madagascar.fossa.dao.RepostRepository;
import com.keepreal.madagascar.fossa.model.RepostInfo;
import com.keepreal.madagascar.fossa.util.RepostCodeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class RepostService {

    private static final String ANDROID_REDIRECT_URL = "/island/home";
    private static final String IOS_REDIRECT_URL = "feeds://island/home";
    private static final String LINK_URL = "/repost?islandId=%s&userId=%s";
    private static final String HOST_TAG = "1";
    private static final String ISLANDER_TAG = "0";

    private final GeneralConfiguration generalConfiguration;
    private final RepostRepository repostRepository;
    private final LongIdGenerator idGenerator;

    public RepostService(GeneralConfiguration generalConfiguration,
                         RepostRepository repostRepository,
                         LongIdGenerator idGenerator) {
        this.generalConfiguration = generalConfiguration;
        this.repostRepository = repostRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Retrieves pageable repost.
     *
     * @param pageRequest   {@link org.springframework.data.domain.Pageable}.
     * @param fromId        feed id or island id.
     * @param type          {@link RepostType}.
     * @return  {@link RepostInfo}.
     */
    public Page<RepostInfo> getRepostInfoPageable(com.keepreal.madagascar.common.PageRequest pageRequest, String fromId, Integer type) {
        return repostRepository.findRepostInfosByFromIdAndFromTypeAndDeletedIsFalse(fromId, type,
                PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize()));
    }

    /**
     * Retrieves repost
     *
     * @param fromId        feed id or island id.
     * @param userId        user id.
     * @param content       repost content.
     * @param isSuccessful  is successful.
     * @param fromType      repost type (feed or island)
     * @return  {@link RepostInfo}.
     */
    public RepostInfo save(String fromId, String userId, String content, Boolean isSuccessful, Integer fromType) {
        return repostRepository.save(RepostInfo.builder()
                .id(String.valueOf(idGenerator.nextId()))
                .fromId(fromId)
                .userId(userId)
                .content(content)
                .successful(isSuccessful)
                .fromType(fromType)
                .build());
    }

    /**
     * Retrieves repost message.
     *
     * @param repostInfo    {@link RepostInfo}.
     * @return  {@link RepostMessage}.
     */
    public RepostMessage getRepostMessage(RepostInfo repostInfo) {
        return RepostMessage.newBuilder()
                .setContent(repostInfo.getContent())
                .setCreatedAt(repostInfo.getCreatedTime())
                .setId(repostInfo.getId())
                .setIsSuccessful(repostInfo.getSuccessful())
                .setUserId(repostInfo.getUserId())
                .build();
    }

    /**
     * Retrieves island repost message.
     *
     * @param repostInfo    {@link RepostInfo}.
     * @return  {@link IslandRepostMessage}.
     */
    public IslandRepostMessage getIslandRepostMessage(RepostInfo repostInfo) {
        if (repostInfo == null) {
            return null;
        }
        RepostMessage repostMessage = getRepostMessage(repostInfo);
        return IslandRepostMessage.newBuilder()
                .setIslandId(repostInfo.getFromId())
                .setIslandRepost(repostMessage)
                .build();
    }

    /**
     * Retrieves feed repost message.
     *
     * @param repostInfo    {@link RepostInfo}.
     * @return  {@link FeedRepostMessage}.
     */
    public FeedRepostMessage getFeedRepostMessage(RepostInfo repostInfo) {
        if (repostInfo == null) {
            return null;
        }
        RepostMessage repostMessage = getRepostMessage(repostInfo);
        return FeedRepostMessage.newBuilder()
                .setFeedId(repostInfo.getFromId())
                .setFeedRepost(repostMessage)
                .build();
    }

    public String generatorCode(IslandMessage islandMessage, String userId, String code, String shortCode) {
        if (userId.equals(islandMessage.getHostId())) {
            return String.format("邀请你加入［%s］\n" +
                    "【复制】这段话$%s$打开跳岛App\n" +
                    "输入暗号［%s］即刻登岛\n" +
                    "或点击链接 %s",
                    islandMessage.getName(), code, islandMessage.getSecret(), String.format(this.generalConfiguration.getShortCodeBase(), shortCode));
        }

        return String.format("邀请你加入［%s］\n" +
                "【复制】这段话$%s$打开跳岛App\n" +
                "或点击链接 %s\n" +
                "暗号接头，限时登岛", islandMessage.getName(), code, String.format(this.generalConfiguration.getShortCodeBase(), shortCode));
    }

    public String getRedirectUrlByDeviceType(DeviceType deviceType) {
        return deviceType.equals(DeviceType.ANDROID) ? ANDROID_REDIRECT_URL : IOS_REDIRECT_URL;
    }

    /**
     * encode with island id and tag(host is 1, islander is 0)
     *
     * @param islandId  island id.
     * @param isHost    is host.
     * @return  code.
     */
    public String encode(String islandId, boolean isHost) {
        String id = isHost ? islandId + HOST_TAG : islandId + ISLANDER_TAG;
        return RepostCodeUtils.encode(id);
    }

    public String decode(String code) {
        String decode = RepostCodeUtils.decode(code);
        return decode.substring(0, decode.length() - 1);
    }

    public boolean isHost(String code) {
        String decode = RepostCodeUtils.decode(code);
        return decode.substring(decode.length() - 1).equals(HOST_TAG);
    }

    public String combineLinkUrl(String islandId, String userId) {
        return String.format(LINK_URL, islandId, userId);
    }

    public String generateShortCode(String url) {
         return RepostCodeUtils.getRandomString();
    }

}
