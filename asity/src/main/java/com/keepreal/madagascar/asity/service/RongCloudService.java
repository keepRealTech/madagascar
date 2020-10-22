package com.keepreal.madagascar.asity.service;

import com.keepreal.madagascar.asity.config.RongCloudConfiguration;
import com.keepreal.madagascar.asity.model.SpecialArtist;
import com.keepreal.madagascar.asity.repository.SpecialArtistsTempRepository;
import com.keepreal.madagascar.common.constants.Constants;
import com.keepreal.madagascar.common.constants.Templates;
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
            String txt =
                    event.getMemberEvent().getPermanent() ?
                            String.format(Templates.ASITY_PURCHASE_HOST_PERMANENT_TEMPLATE,
                                    event.getMemberEvent().getMembershipName(),
                                    Long.valueOf(event.getMemberEvent().getPriceInCents()).doubleValue() / 100 / event.getMemberEvent().getTimeInMonths()) :
                            String.format(Templates.ASITY_PURCHASE_HOST_TEMPLATE,
                                    event.getMemberEvent().getMembershipName(),
                                    Long.valueOf(event.getMemberEvent().getPriceInCents()).doubleValue() / 100 / event.getMemberEvent().getTimeInMonths(),
                                    event.getMemberEvent().getTimeInMonths());
            this.sendMessage(event.getMemberEvent().getMemberId(), new String[]{event.getUserId()}, txt, 1);
        }

        String txt;
        if (!event.hasMemberEvent() || !membership.getUseCustomMessage()) {
            txt = Templates.ASITY_PURCHASE_CUSTOMER_TEMPLATE;
        } else {
            txt = membership.getMessage();
        }

        String targetId;
        if (event.hasMemberEvent()) {
            targetId = event.getMemberEvent().getMemberId();
        } else if (event.hasFeedPaymentEvent()) {
            targetId = event.getFeedPaymentEvent().getUserId();
        } else {
            return;
        }

        this.sendMessage(event.getUserId(), new String[]{targetId}, txt, this.isSpecialArtist(event.getUserId()) ? 0 : 1);
    }

    /**
     * Sends simple text message.
     *
     * @param targetIds Target user ids.
     * @param text      Text.
     */
    @SneakyThrows
    public void sendMessage(String[] targetIds, String text) {
        this.sendMessage(Constants.OFFICIAL_USER_ID, targetIds, text, 0);
    }

    /**
     * Sends simple text message.
     *
     * @param senderId      Sender user id.
     * @param targetIds     Target user ids.
     * @param text          Text.
     * @param includeSender Whether includes the sender, 0 exclude : 1 include.
     */
    @SneakyThrows
    public void sendMessage(String senderId, String[] targetIds, String text, int includeSender) {
        TxtMessage txtMessage = new TxtMessage(text, "");

        PrivateMessage message = new PrivateMessage()
                .setSenderId(senderId)
                .setTargetId(targetIds)
                .setObjectName(txtMessage.getType())
                .setContent(txtMessage)
                .setVerifyBlacklist(0)
                .setIsPersisted(0)
                .setIsCounted(0)
                .setIsIncludeSender(includeSender);
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
