package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.dao.UserDeviceInfoRepository;
import com.keepreal.madagascar.coua.model.SimpleDeviceToken;
import com.keepreal.madagascar.coua.model.UserDeviceInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Represents the user device service.
 */
@Service
public class UserDeviceInfoService {

    private final UserDeviceInfoRepository userDeviceInfoRepository;
    private final LongIdGenerator idGenerator;

    /**
     * Constructs the user device info service.
     *
     * @param userDeviceInfoRepository  {@link UserDeviceInfoRepository}.
     * @param idGenerator               {@link LongIdGenerator}.
     */
    public UserDeviceInfoService(UserDeviceInfoRepository userDeviceInfoRepository,
                                 LongIdGenerator idGenerator) {
        this.userDeviceInfoRepository = userDeviceInfoRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * bind device token.
     *
     * @param userId        user id.
     * @param deviceToken   device token.
     */
    public void bindDeviceToken(String userId, String deviceToken, Integer deviceType) {
        UserDeviceInfo userDeviceInfo = userDeviceInfoRepository.findByUserIdAndDeviceTokenAndDeviceTypeAndDeletedIsFalse(userId, deviceToken, deviceType);
        if (userDeviceInfo != null) {
            return;
        }
        userDeviceInfo = new UserDeviceInfo();
        userDeviceInfo.setId(String.valueOf(idGenerator.nextId()));
        userDeviceInfo.setUserId(userId);
        userDeviceInfo.setBinded(true);
        userDeviceInfo.setDeviceToken(deviceToken);
        userDeviceInfo.setDeviceType(deviceType);
        userDeviceInfo.setUpdatedTime(System.currentTimeMillis());
        userDeviceInfoRepository.save(userDeviceInfo);
    }

    /**
     * unbind device token.
     *
     * @param userId        user id.
     * @param deviceToken   device token.
     */
    public void unbindDeviceToken(String userId, String deviceToken, Integer deviceType) {
        UserDeviceInfo userDeviceInfo = userDeviceInfoRepository.findByUserIdAndDeviceTokenAndDeviceTypeAndDeletedIsFalse(userId, deviceToken, deviceType);
        if (userDeviceInfo != null) {
            userDeviceInfo.setBinded(false);
            userDeviceInfoRepository.save(userDeviceInfo);
        }
    }

    /**
     * retrieve device token list by user id list.
     *
     * @param userIdList    user id list.
     * @return  device token list.
     */
    public List<SimpleDeviceToken> getDeviceTokenListByUserIdList(List<String> userIdList) {
        return userDeviceInfoRepository.findDeviceTokenListByUserIdList(userIdList);
    }

    /**
     * retrieve device token by user id.
     *
     * @param userId    user id.
     * @return  device token list.
     */
    public List<SimpleDeviceToken> getDeviceTokenByUserId(String userId) {
        return userDeviceInfoRepository.findDeviceTokensByUserId(userId);
    }
}
