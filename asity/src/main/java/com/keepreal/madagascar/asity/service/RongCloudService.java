package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.config.RongCloudConfiguration;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.MembershipMessage;
import com.keepreal.madagascar.coua.CreateIslandEvent;
import com.keepreal.madagascar.tenrecs.NotificationEvent;
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

import java.util.Objects;

/**
 * Represents the rong cloud service.
 */
@Service
@Slf4j
public class RongCloudService {

    private static final String HOST_TEMPLATE = "我刚刚支持了你的「%s」会员（¥%.2f x %d个月）。创作加油！";
    private static final String MEMBER_TEMPLATE = "感谢你的支持！\n更多信息可前往「我的 - 订单中心」中查看。";
    private static final String OFFICIAL_USER_ID = "4";
    private static final String CREATE_ISLAND_CONTENT = "恭喜你成功创建了你的主页！现在可以在手机端发布动态、网页端上传作品或分享主页链接获取支持了。\n" +
            "\n" +
            "你还可以：\n" +
            "1. 前往「我的-创作者认证」申请认证。认证可以防止你被冒充，获得跳岛首页推荐的机会，以及不定期的创作鼓励补贴。\n" +
            "2. 阅读《创作者指南》，以便更好的使用跳岛。\n" +
            "\n" +
            "欢迎来官方微博给我留言跟我互动哦～如遇到问题可在设置内-咨询跳岛小客服。";
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
     * @param event      {@link NotificationEvent}.
     * @param membership {@link MembershipMessage}.
     */
    @SneakyThrows
    public void sendThanks(NotificationEvent event, MembershipMessage membership) {
        TxtMessage hostTextMessage = new TxtMessage(
                String.format(RongCloudService.HOST_TEMPLATE,
                        event.getMemberEvent().getMembershipName(),
                        Long.valueOf(event.getMemberEvent().getPriceInCents()).doubleValue() / 100,
                        event.getMemberEvent().getTimeInMonths()),
                "");
        PrivateMessage hostMessage = new PrivateMessage()
                .setSenderId(event.getMemberEvent().getMemberId())
                .setTargetId(new String[]{event.getUserId()})
                .setObjectName(hostTextMessage.getType())
                .setContent(hostTextMessage)
                .setVerifyBlacklist(0)
                .setIsPersisted(0)
                .setIsCounted(0)
                .setIsIncludeSender(1);
        this.client.message.msgPrivate.send(hostMessage);


        TxtMessage memberTextMessage;
        if (Objects.isNull(membership) || !membership.getUseCustomMessage()) {
            memberTextMessage = new TxtMessage(
                    String.format(RongCloudService.MEMBER_TEMPLATE,
                            event.getMemberEvent().getMembershipName()),
                    "");
        } else {
            memberTextMessage = new TxtMessage(
                    membership.getMessage(),
                    "");
        }

        PrivateMessage memberMessage = new PrivateMessage()
                .setSenderId(event.getUserId())
                .setTargetId(new String[]{event.getMemberEvent().getMemberId()})
                .setObjectName(memberTextMessage.getType())
                .setContent(memberTextMessage)
                .setVerifyBlacklist(0)
                .setIsPersisted(0)
                .setIsCounted(0)
                .setIsIncludeSender(1);
        this.client.message.msgPrivate.send(memberMessage);
    }

    @SneakyThrows
    public void sentCreateIslandNotice(CreateIslandEvent createIslandEvent) {
        TxtMessage txtMessage = new TxtMessage(CREATE_ISLAND_CONTENT, "");

        PrivateMessage message = new PrivateMessage()
                .setSenderId(OFFICIAL_USER_ID)
                .setTargetId(new String[]{createIslandEvent.getHostId()})
                .setObjectName(txtMessage.getType())
                .setContent(txtMessage)
                .setVerifyBlacklist(0)
                .setIsPersisted(0)
                .setIsCounted(0)
                .setIsIncludeSender(1);
        this.client.message.msgPrivate.send(message);
    }

}
