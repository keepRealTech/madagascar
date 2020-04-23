package com.keepreal.dao;

import com.keepreal.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-22
 **/

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

    UserInfo findByUserId(String userId);
}
