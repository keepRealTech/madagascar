package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.config.RongCloudConfiguration;
import com.keepreal.madagascar.asity.model.SpecialArtist;
import com.keepreal.madagascar.asity.repository.SpecialArtistsTempRepository;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.coua.CreateUserEvent;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents the rong cloud service.
 */
@Service
@Slf4j
public class RongCloudService {

    private static final String HOST_TEMPLATE = "我刚刚支持了你的「%s」会员（¥%.2f x %d个月）。创作加油！";
    private static final String HOST_PERMANENT_TEMPLATE = "我刚刚支持了你的「%s」会员（¥%.2f x 永久有效）。创作加油！";
    private static final String MEMBER_TEMPLATE = "感谢你的支持！\n更多信息可前往「我的 - 订单中心」中查看。";
    private static final String OFFICIAL_USER_ID = "4";
    private static final String CREATE_ISLAND_CONTENT = "创作者你好，恭喜成功创建了你的跳岛主页！\n" +
            "\n" +
            "你可以：在手机端发布图文动态、创建群组、管理提问箱。在网页端上传文章、高清大图、音频和视频。快分享主页链接来获取支持吧～\n" +
            "\n" +
            "你还可以：阅读《创作者指南》，以便更好的使用跳岛： https://mp.weixin.qq.com/s/7uarq2vJOnt0VPVWfHCb_g\n" +
            "\n" +
            "欢迎来官方微博给我留言跟我互动哦～如遇到问题可在【设置】-【反馈与帮助】内咨询跳岛小客服。";

    private static final String CREATE_USER_CONTENT = "欢迎登岛！我是跳岛管理员“岛蛋”！跳岛是帮助创作者获取支持以维持创作的平台。\n" +
            "\n" +
            "跳岛团队中有音乐视频达人、个人博主、独立乐队发烧友...我们深知创作不易，更坚信为内容世界创造价值的人本应得到回报，跳岛为此而生。创作不是一座孤岛，还有支持者的陪伴，愿大家在岛上玩得开心！\n" +
            "\n" +
            "如果你是创作者：\n" +
            "点击「我的-成为创作者」，跟随提示设置创作主页和支持方案，分享获得支持。\n" +
            "\n" +
            "如果你是支持者：\n" +
            "搜索你喜爱的创作者为他[支持一下]，订购他的支持方案享受对应权益或回馈。或是发现更多有趣好玩的创作。\n" +
            "\n" +
            "欢迎来官方微博给我留言跟我互动哦～如遇到问题可在设置内-咨询跳岛小客服";
    private static final String CREATE_USER_CONTENT_TEMP = "🎁【有奖问卷】快来填问卷拿礼物吧！\n" +
            "欢迎登岛！这里有一份新用户小问卷，非常希望百忙之中您能帮忙填一下！\n" +
            "岛蛋会有随机红包奖励呦，请提交问卷后添加官方微信领取：tiaodaoapp \n" +
            "问卷地址：https://www.wenjuan.com/s/UZBZJvzgDu/\n" +
            "关于跳岛的有奖问答\n" +
            "感谢您能抽出几分钟时间来帮助我们做问答，我们会从中抽取奖品哦!\n";
    private final RongCloud client;

    private final SpecialArtistsTempRepository specialArtistsTempRepository;

    /**
     * Constructs the rong cloud service.
     *
     * @param rongCloudConfiguration {@link RongCloudConfiguration}.
     * @param specialArtistsTempRepository {@link SpecialArtistsTempRepository}
     */
    public RongCloudService(RongCloudConfiguration rongCloudConfiguration,
                            SpecialArtistsTempRepository specialArtistsTempRepository) {
        this.client = RongCloud.getInstance(rongCloudConfiguration.getAppKey(), rongCloudConfiguration.getAppSecret());
        this.specialArtistsTempRepository = specialArtistsTempRepository;
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
     * Tries to update the user info in rong cloud.
     *
     * @param userId      User id.
     * @param userName    User name.
     * @param portraitUrl Portrait url.
     */
    @SneakyThrows
    public void updateUser(String userId, String userName, String portraitUrl) {
        UserModel userModel = new UserModel()
                .setId(userId)
                .setName(userName)
                .setPortrait(portraitUrl);

        Result refreshResult = this.client.user.update(userModel);

        if (!refreshResult.getCode().equals(200)) {
            log.warn("Failed to update user info for {}", userId);
        }
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
        if (event.hasMemberEvent() && !isSpecialArtist(event.getUserId())) {
            TxtMessage hostTextMessage = new TxtMessage(
                    event.getMemberEvent().getPermanent() ?
                            String.format(RongCloudService.HOST_PERMANENT_TEMPLATE,
                                    event.getMemberEvent().getMembershipName(),
                                    Long.valueOf(event.getMemberEvent().getPriceInCents()).doubleValue() / 100 / event.getMemberEvent().getTimeInMonths()) :
                            String.format(RongCloudService.HOST_TEMPLATE,
                                    event.getMemberEvent().getMembershipName(),
                                    Long.valueOf(event.getMemberEvent().getPriceInCents()).doubleValue() / 100 / event.getMemberEvent().getTimeInMonths(),
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
        }

        TxtMessage memberTextMessage;
        if (!event.hasMemberEvent() || !membership.getUseCustomMessage()) {
            memberTextMessage = new TxtMessage(
                    RongCloudService.MEMBER_TEMPLATE,
                    "");
        } else {
            memberTextMessage = new TxtMessage(
                    membership.getMessage(),
                    "");
        }

        String targetId;
        if (event.hasMemberEvent()) {
            targetId = event.getMemberEvent().getMemberId();
        } else if (event.hasFeedPaymentEvent()) {
            targetId = event.getFeedPaymentEvent().getUserId();
        } else {
            return;
        }

        PrivateMessage memberMessage = new PrivateMessage()
                .setSenderId(event.getUserId())
                .setTargetId(new String[]{targetId})
                .setObjectName(memberTextMessage.getType())
                .setContent(memberTextMessage)
                .setVerifyBlacklist(0)
                .setIsPersisted(0)
                .setIsCounted(0)
                .setIsIncludeSender(this.isSpecialArtist(event.getUserId()) ? 0 : 1);
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
                .setIsIncludeSender(0);
        this.client.message.msgPrivate.send(message);
    }

    @SneakyThrows
    public void sentCreateUserNotice(CreateUserEvent createUserEvent) {
        TxtMessage txtMessage = new TxtMessage(CREATE_USER_CONTENT, "");

        PrivateMessage message = new PrivateMessage()
                .setSenderId(OFFICIAL_USER_ID)
                .setTargetId(new String[]{createUserEvent.getUserId()})
                .setObjectName(txtMessage.getType())
                .setContent(txtMessage)
                .setVerifyBlacklist(0)
                .setIsPersisted(0)
                .setIsCounted(0)
                .setIsIncludeSender(0);
        this.client.message.msgPrivate.send(message);
    }

    @SneakyThrows
    public void sentCreateUserNoticeTemp(CreateUserEvent createUserEvent) {
        TxtMessage txtMessage = new TxtMessage(CREATE_USER_CONTENT_TEMP, "");

        PrivateMessage message = new PrivateMessage()
                .setSenderId(OFFICIAL_USER_ID)
                .setTargetId(new String[]{createUserEvent.getUserId()})
                .setObjectName(txtMessage.getType())
                .setContent(txtMessage)
                .setVerifyBlacklist(0)
                .setIsPersisted(0)
                .setIsCounted(0)
                .setIsIncludeSender(0);
        this.client.message.msgPrivate.send(message);
    }

    /**
     * 判断是否为特殊创作者
     *
     * @param artistUserId 创作者 user id
     * @return 是返回true
     */
    private boolean isSpecialArtist(String artistUserId) {
        SpecialArtist artist = this.specialArtistsTempRepository.findTopByIdAndDeletedIsFalse(artistUserId);
        return Objects.nonNull(artist);
    }
}
