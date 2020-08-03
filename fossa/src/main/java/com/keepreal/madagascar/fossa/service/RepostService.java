package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.DeviceType;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.common.RepostMessage;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.FeedRepostMessage;
import com.keepreal.madagascar.fossa.IslandRepostMessage;
import com.keepreal.madagascar.fossa.dao.RepostRepository;
import com.keepreal.madagascar.fossa.model.RepostInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class RepostService {

    private static final String ANDROID_REDIRECT_URL = "";
    private static final String IOS_REDIRECT_URL = "";
    private static final String LINKED_URL = "";

    private final RepostRepository repostRepository;
    private final LongIdGenerator idGenerator;

    public RepostService(RepostRepository repostRepository,
                         LongIdGenerator idGenerator) {
        this.repostRepository = repostRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Retrieves pageable repost.
     *
     * @param pageRequest   {@link org.springframework.data.domain.Pageable}.
     * @param fromId        feed id or island id.
     * @return  {@link RepostInfo}.
     */
    public Page<RepostInfo> getRepostInfoPageable(com.keepreal.madagascar.common.PageRequest pageRequest, String fromId) {
        return repostRepository.findRepostInfosByFromId(fromId, PageRequest.of(pageRequest.getPage(), pageRequest.getPageSize()));
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

    public String generatorCode(IslandMessage islandMessage, String userId, String code) {
        if (userId.equals(islandMessage.getHostId())) {
            return String.format("邀请你加入［%s］\n" +
                    "【复制】这段话$%s$打开跳岛App\n" +
                    "输入暗号［%s］即刻登岛\n" +
                    "或点击链接%s",
                    islandMessage.getName(), code, islandMessage.getSecret(), LINKED_URL);
        }

        return String.format("邀请你加入［%s］\n" +
                "【复制】这段话$%s$打开跳岛App\n" +
                "或点击链接%s\n" +
                "暗号接头，限时登岛", islandMessage.getName(), code, LINKED_URL);
    }

    public String getRedirectUrlByDeviceType(DeviceType deviceType) {
        return deviceType.equals(DeviceType.ANDROID) ? ANDROID_REDIRECT_URL : IOS_REDIRECT_URL;
    }
}