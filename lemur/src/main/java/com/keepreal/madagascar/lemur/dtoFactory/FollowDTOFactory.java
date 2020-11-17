package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.angonoka.FollowState;
import com.keepreal.madagascar.angonoka.FollowType;
import com.keepreal.madagascar.angonoka.RetrieveAllSuperFollowResponse;
import com.keepreal.madagascar.angonoka.SuperFollowMessage;
import com.keepreal.madagascar.angonoka.WeiboProfileMessage;
import com.keepreal.madagascar.common.constants.Templates;
import com.keepreal.madagascar.lemur.service.FollowService;
import com.keepreal.madagascar.lemur.service.MpWechatService;
import com.keepreal.madagascar.lemur.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import swagger.model.FollowBotState;
import swagger.model.FollowDTO;
import swagger.model.FollowInfoDTO;
import swagger.model.WeiboProfileDTO;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the follow dto factory.
 */
@Component
public class FollowDTOFactory {
    private final UserService userService;
    private final MpWechatService mpWechatService;
    private final FollowService followService;

    public FollowDTOFactory(UserService userService,
                            MpWechatService mpWechatService,
                            FollowService followService) {
        this.userService = userService;
        this.mpWechatService = mpWechatService;
        this.followService = followService;
    }

    /**
     * Converts the {@link WeiboProfileMessage} to the {@link WeiboProfileDTO}
     *
     * @return {@link WeiboProfileDTO}
     */
    public WeiboProfileDTO valueOf (WeiboProfileMessage message) {
        if (Objects.isNull(message)) {
            return null;
        }
        WeiboProfileDTO dto = new WeiboProfileDTO();
        dto.setId(message.getId());
        dto.setName(message.getName());
        dto.setFollowerCount(message.getFollowerCount());
        dto.setAvatarUrl(message.getAvatarUrl());
        return dto;
    }

    public FollowInfoDTO valueOf(RetrieveAllSuperFollowResponse response) {
        FollowInfoDTO followInfoDTO = new FollowInfoDTO();
        followInfoDTO.setWeibo(this.valueOf(response.getWeibo(), FollowType.FOLLOW_WEIBO));
        followInfoDTO.setBilibili(this.valueOf(response.getBilibili(), FollowType.FOLLOW_BILIBILI));
        followInfoDTO.setTiktok(this.valueOf(response.getTiktok(), FollowType.FOLLOW_TIKTOK));
        return followInfoDTO;
    }

    public FollowDTO valueOf(SuperFollowMessage message, FollowType followType) {
        FollowDTO followDTO = new FollowDTO();

        if (Objects.isNull(message) || StringUtils.isEmpty(message.getId())) {
            followDTO.setState(FollowBotState.NONE);
            followDTO.setCode("");
            followDTO.setTicket("");
            followDTO.setName("");
            followDTO.setRunningDays(0L);
            followDTO.setDescription(String.format(Templates.GET_FOLLOW_CONTENT, this.convertStateNone(followType)));
            return followDTO;
        }

        followDTO.setCode(message.getCode());
        followDTO.setState(this.valueOf(message.getState()));
        //暂时只有微博有
        String name = this.followService.retrieveWeiboProfileByCondition(null, message.getPlatformId()).getName();
        followDTO.setName(name);
        followDTO.setDescription(String.format(Templates.GET_FOLLOW_CONTENT, this.convertStateEnabled(followType, name)));
        followDTO.setRunningDays(this.calculateRunningDays(message.getCreatedTime()));
        followDTO.setTicket(String.format(Templates.MP_WECHAT_QRCODE_URL,this.mpWechatService.retrievePermanentQRCode()));
        return followDTO;
    }

    public FollowBotState valueOf(FollowState followType) {
        switch (followType) {
            case ENABLE:
                return FollowBotState.ENABLED;
            case SUSPEND:
                return FollowBotState.SUSPEND;
            case NONE:
                return FollowBotState.NONE;
            default:
               return null;
        }
    }

    private Long calculateRunningDays(long createdTime) {
        long now = Instant.now().toEpochMilli();
        long runningDays = now - createdTime;
        if (runningDays < 2 * 24 * 60 * 60 * 1000L) {
            return 1L;
        }
        return runningDays / (24 * 60 * 60 * 1000L);
    }

    private String convertStateNone(FollowType followType) {
        switch (followType) {
            case FOLLOW_WEIBO:
                return "岛主微博账号作品";
            case FOLLOW_TIKTOK:
                return "岛主抖音账号作品";
            case FOLLOW_BILIBILI:
                return "岛主B站账号作品";
            default:
                return "";
        }
    }

    private String convertStateEnabled(FollowType followType, String name) {
        switch (followType) {
            case FOLLOW_WEIBO:
                return "微博账号@" + name;
            case FOLLOW_TIKTOK:
                return "抖音账号@" + name;
            case FOLLOW_BILIBILI:
                return "B站账号@" + name;
            default:
                return "";
        }
    }

}
