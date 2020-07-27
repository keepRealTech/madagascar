package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.config.RongCloudConfiguration;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import io.rong.RongCloud;
import io.rong.messages.TxtMessage;
import io.rong.models.Result;
import io.rong.models.group.GroupMember;
import io.rong.models.group.GroupModel;
import io.rong.models.message.PrivateMessage;
import io.rong.models.response.TokenResult;
import io.rong.models.user.UserModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Represents the rong cloud service.
 */
@Service
@Slf4j
public class RongCloudService {

    private static final TxtMessage txtMessage = new TxtMessage("感谢你的支持～", "");
    private final RongCloud client;

    /**
     * Constructs the rong cloud service.
     *
     * @param rongCloudConfiguration {@link RongCloudConfiguration}.
     */
    public RongCloudService(RongCloudConfiguration rongCloudConfiguration) {
        this.client = RongCloud.getInstance(rongCloudConfiguration.getAppKey(), rongCloudConfiguration.getAppSecret());
    }

    /**
     * Registers user and returns token.
     *
     * @param userId      User id.
     * @param userName    User name.
     * @param portraitUrl User portrait url.
     * @return User token.
     */
    @SneakyThrows
    public String register(String userId, String userName, String portraitUrl) {
        userName = userName.replace((char) 12288, ' ');
        if (StringUtils.isEmpty(userName.trim())) {
            userName = "user " + userId;
        }
        UserModel userModel = new UserModel()
                .setId(userId)
                .setName(userName)
                .setPortrait(portraitUrl);
        TokenResult tokenResult = this.client.user.register(userModel);

        if (!tokenResult.getCode().equals(200)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_RONGCLOUD_RPC_ERROR);
        }

        return tokenResult.getToken();
    }

    /**
     * Creates a new group chat.
     *
     * @param id     Group id.
     * @param name   Group name.
     * @param hostId Group host id.
     */
    @SneakyThrows
    public void createGroup(String id, String name, String hostId) {
        GroupMember[] members = {new GroupMember().setId(hostId)};
        GroupModel groupModel = new GroupModel()
                .setId(id)
                .setName(name)
                .setMembers(members);
        Result result = this.client.group.create(groupModel);

        if (!result.getCode().equals(200)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_RONGCLOUD_RPC_ERROR);
        }
    }

    /**
     * Dismisses a group chat.
     *
     * @param id     Group id.
     * @param hostId User who dismisses the group.
     */
    @SneakyThrows
    public void dismissGroup(String id, String hostId) {
        GroupMember[] members = {new GroupMember().setId(hostId)};
        GroupModel groupModel = new GroupModel()
                .setId(id)
                .setMembers(members);
        Result result = this.client.group.dismiss(groupModel);

        if (!result.getCode().equals(200)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_RONGCLOUD_RPC_ERROR);
        }
    }

    /**
     * Joins a group chat.
     *
     * @param id        Group id.
     * @param groupName Group name.
     * @param memberId  Member user id.
     */
    @SneakyThrows
    public void joinGroup(String id, String groupName, String memberId) {
        GroupMember[] members = {new GroupMember().setId(memberId)};
        GroupModel groupModel = new GroupModel()
                .setId(id)
                .setName(groupName)
                .setMembers(members);
        Result result = this.client.group.join(groupModel);

        if (!result.getCode().equals(200)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_RONGCLOUD_RPC_ERROR);
        }
    }

    /**
     * Quits a group chat.
     *
     * @param id       Group id.
     * @param memberId Member user id.
     */
    @SneakyThrows
    public void quitGroup(String id, String memberId) {
        GroupMember[] members = {new GroupMember().setId(memberId)};
        GroupModel groupModel = new GroupModel()
                .setId(id)
                .setMembers(members);
        Result result = this.client.group.quit(groupModel);

        if (!result.getCode().equals(200)) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_RONGCLOUD_RPC_ERROR);
        }
    }

    /**
     * Sends a private thank you message.
     *
     * @param userId   User id.
     * @param targetId Target id.
     */
    @SneakyThrows
    public void sendThanks(String userId, String targetId) {
        PrivateMessage privateMessage = new PrivateMessage()
                .setSenderId(userId)
                .setTargetId(new String[]{targetId})
                .setObjectName(RongCloudService.txtMessage.getType())
                .setContent(RongCloudService.txtMessage)
                .setVerifyBlacklist(0)
                .setIsPersisted(0)
                .setIsCounted(0)
                .setIsIncludeSender(1);
        this.client.message.msgPrivate.send(privateMessage);
    }

}
