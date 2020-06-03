package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.dao.UserDeviceInfoRepository;
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
     * @param idGenerator
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
    public void bindDeviceToken(String userId, String deviceToken) {
        UserDeviceInfo userDeviceInfo = new UserDeviceInfo();
        userDeviceInfo.setId(String.valueOf(idGenerator.nextId()));
        userDeviceInfo.setUserId(userId);
        userDeviceInfo.setBinded(true);
        userDeviceInfo.setDeviceToken(deviceToken);
        userDeviceInfoRepository.save(userDeviceInfo);
    }

    /**
     * unbind device token.
     *
     * @param userId        user id.
     * @param deviceToken   device token.
     */
    public void unbindDeviceToken(String userId, String deviceToken) {
        UserDeviceInfo userDeviceInfo = userDeviceInfoRepository.findByUserIdAndDeviceTokenAndDeletedIsFalse(userId, deviceToken);
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
    public List<String> getDeviceTokenListByUserIdList(List<String> userIdList) {
        return userDeviceInfoRepository.findDeviceTokenListByUserIdList(userIdList);
    }
}
