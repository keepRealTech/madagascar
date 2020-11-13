package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.angonoka.FollowState;
import com.keepreal.madagascar.angonoka.RetrieveAllSuperFollowResponse;
import com.keepreal.madagascar.angonoka.SuperFollowMessage;
import com.keepreal.madagascar.angonoka.WeiboProfileMessage;
import com.keepreal.madagascar.common.constants.Templates;
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

    public FollowDTOFactory(UserService userService,
                            MpWechatService mpWechatService) {
        this.userService = userService;
        this.mpWechatService = mpWechatService;
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
        followInfoDTO.setWeibo(this.valueOf(response.getWeibo()));
        followInfoDTO.setBilibili(this.valueOf(response.getBilibili()));
        followInfoDTO.setTiktok(this.valueOf(response.getTiktok()));
        return followInfoDTO;
    }

    public FollowDTO valueOf(SuperFollowMessage message) {
        FollowDTO followDTO = new FollowDTO();

        if (Objects.isNull(message) || StringUtils.isEmpty(message.getId())) {
            followDTO.setState(FollowBotState.NONE);
            return followDTO;
        }

        followDTO.setCode(message.getCode());
        followDTO.setState(this.valueOf(message.getState()));
        followDTO.setName(this.userService.retrieveUserById(message.getHostId()).getName());
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

}
