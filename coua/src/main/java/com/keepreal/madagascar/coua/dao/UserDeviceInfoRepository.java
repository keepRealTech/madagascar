package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.SimpleDeviceToken;
import com.keepreal.madagascar.coua.model.UserDeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDeviceInfoRepository extends JpaRepository<UserDeviceInfo, String> {

    UserDeviceInfo findByUserIdAndDeviceTokenAndDeviceTypeAndDeletedIsFalse(String userId, String deviceToken, Integer deviceType);

    @Query(value = "SELECT device_token AS deviceToken, device_type AS deviceType FROM user_device WHERE user_id IN ?1 AND is_binded = TRUE AND is_deleted = FALSE", nativeQuery = true)
    List<SimpleDeviceToken> findDeviceTokenListByUserIdList(List<String> userIdList);

    @Query(value = "SELECT device_token AS deviceToken, device_type AS deviceType FROM user_device WHERE user_id = ?1 AND is_binded = TRUE AND is_deleted = FALSE", nativeQuery = true)
    List<SimpleDeviceToken> findDeviceTokensByUserId(String userId);

    List<UserDeviceInfo> findAllByDeviceTokenAndDeletedIsFalse(String deviceToken);
}
