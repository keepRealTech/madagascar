package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    UserInfo findUserInfoByIdAndDeletedIsFalse(Long id);

    UserInfo findUserInfoByUnionIdAndDeletedIsFalse(String unionId);
}
