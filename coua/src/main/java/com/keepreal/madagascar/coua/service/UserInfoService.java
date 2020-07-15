package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.Gender;
import com.keepreal.madagascar.common.IdentityType;
import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.common.DisplayIdGenerator;
import com.keepreal.madagascar.coua.dao.UserInfoRepository;
import com.keepreal.madagascar.coua.model.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents user service.
 */
@Service
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final UserIdentityService userIdentityService;
    private final BalanceService balanceService;
    private final LongIdGenerator idGenerator;
    private final DisplayIdGenerator displayIdGenerator;

    /**
     * Constructs user service.
     *
     * @param userInfoRepository  {@link UserInfoRepository}.
     * @param userIdentityService {@link UserIdentityService}.
     * @param balanceService      {@link BalanceService}.
     * @param idGenerator         {@link LongIdGenerator}.
     * @param displayIdGenerator  {@link DisplayIdGenerator}.
     */
    public UserInfoService(UserInfoRepository userInfoRepository,
                           UserIdentityService userIdentityService,
                           BalanceService balanceService,
                           LongIdGenerator idGenerator,
                           DisplayIdGenerator displayIdGenerator) {
        this.userInfoRepository = userInfoRepository;
        this.userIdentityService = userIdentityService;
        this.balanceService = balanceService;
        this.idGenerator = idGenerator;
        this.displayIdGenerator = displayIdGenerator;
    }

    /**
     * Set userId and displayId and save.
     *
     * @param userInfo {@link UserInfo}.
     * @return {@link UserInfo}.
     */
    public UserInfo createUser(UserInfo userInfo) {
        userInfo.setId(String.valueOf(idGenerator.nextId()));
        userInfo.setDisplayId(displayIdGenerator.nextDisplayId());
        userInfo = userInfoRepository.save(userInfo);

        this.balanceService.createBalanceByUserId(userInfo.getId());

        return userInfo;
    }

    public UserInfo updateUser(UserInfo userInfo) {
        return userInfoRepository.save(userInfo);
    }

    /**
     * Retrieve userMessage by userId.
     *
     * @param userId user id.
     * @return {@link UserMessage}.
     */
    public UserMessage getUserMessageById(String userId) {
        UserInfo userInfo = userInfoRepository.findUserInfoByIdAndDeletedIsFalse(userId);
        if (userInfo == null) {
            return null;
        }
        return getUserMessage(userInfo);
    }

    /**
     * Retrieve userMessageList by userIdList.
     *
     * @param userIdList user id list.
     * @return {@link UserMessage}.
     */
    public List<UserMessage> getUserMessageListByIdList(List<String> userIdList) {
        List<UserInfo> userInfoList = userInfoRepository.findUserInfoInfosByIdInAndDeletedIsFalse(userIdList);
        return userInfoList.stream().map(this::getUserMessage).collect(Collectors.toList());
    }

    /**
     * Retrieve userMessage.
     *
     * @param userInfo {@link UserInfo}.
     * @return {@link UserMessage}.
     */
    public UserMessage getUserMessage(UserInfo userInfo) {
        List<Integer> identities = userIdentityService.getAllIdentitiesByUserId(userInfo.getId());
        List<IdentityType> identityTypes = identities.stream().map(IdentityType::forNumber).collect(Collectors.toList());
        return UserMessage.newBuilder()
                .setId(userInfo.getId())
                .setDisplayId(userInfo.getDisplayId())
                .setName(userInfo.getNickName())
                .setPortraitImageUri(userInfo.getPortraitImageUri())
                .setGender(Gender.forNumber(userInfo.getGender()))
                .setDescription(userInfo.getDescription())
                .setCity(userInfo.getCity())
                .setBirthday(userInfo.getBirthday().toString())
                .setUnionId(userInfo.getUnionId())
                .addAllIdentities(identityTypes)
                .setUsername(StringUtils.isEmpty(userInfo.getUsername()) ? "" : userInfo.getUsername())
                .setPassword(StringUtils.isEmpty(userInfo.getPassword()) ? "" : userInfo.getPassword())
                .setCreatedAt(userInfo.getCreatedTime())
                .setLocked(Instant.now().toEpochMilli() <= userInfo.getLockedUntil())
                .build();
    }

    /**
     * Retrieve {@link UserInfo} by userId.
     *
     * @param userId userId.
     * @return {@link UserInfo}.
     */
    public UserInfo findUserInfoByIdAndDeletedIsFalse(String userId) {
        return userInfoRepository.findUserInfoByIdAndDeletedIsFalse(userId);
    }

    /**
     * Retrieve {@link UserInfo} by unionId.
     *
     * @param unionId unionId.
     * @return {@link UserInfo}.
     */
    public UserInfo findUserInfoByUnionIdAndDeletedIsFalse(String unionId) {
        return userInfoRepository.findUserInfoByUnionIdAndDeletedIsFalse(unionId);
    }

    /**
     * Retrieve {@link UserInfo} by displayId.
     *
     * @param displayId displayId.
     * @return {@link UserInfo}.
     */
    public UserInfo findUserInfoByDisplayIdAndDeletedIsFalse(String displayId) {
        return userInfoRepository.findUserInfoByDisplayIdAndDeletedIsFalse(displayId);
    }

    /**
     * Retrieve {@link UserInfo} by username.
     *
     * @param username Username.
     * @return {@link UserInfo}.
     */
    public UserInfo findUserInfoByUserNameAndDeletedIsFalse(String username) {
        return userInfoRepository.findTopByUsernameAndDeletedIsFalse(username);
    }

    /**
     * Retrieve {@link UserInfo} by ids.
     *
     * @param ids User ids.
     * @return {@link UserInfo}.
     */
    public List<UserInfo> findUserInfosByIds(Iterable<String> ids) {
        return this.userInfoRepository.findAllByIdInAndDeletedIsFalse(ids);
    }

}
