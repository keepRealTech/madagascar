package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.UserDeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDeviceInfoRepository extends JpaRepository<UserDeviceInfo, String> {

    UserDeviceInfo findByUserIdAndDeviceTokenAndDeletedIsFalse(String userId, String deviceToken);

    @Query(value = "SELECT device_token FROM uesr_device WHERE user_id IN ?1 AND is_binded = FALSE AND is_deleted = FALSE", nativeQuery = true)
    List<String> findDeviceTokenListByUserIdList(List<String> userIdList);
}
