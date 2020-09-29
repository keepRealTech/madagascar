package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

    UserInfo findUserInfoByIdAndDeletedIsFalse(String id);

    UserInfo findUserInfoByUnionIdAndDeletedIsFalse(String unionId);

    UserInfo findUserInfoByDisplayIdAndDeletedIsFalse(String displayId);

    List<UserInfo> findAllByIdInAndDeletedIsFalse(Iterable<String> ids);

    boolean existsByDisplayId(String displayId);

    UserInfo findTopByUsernameAndDeletedIsFalse(String username);

    @Query(value =
            "SELECT id, display_id, nick_name, portrait_image_uri, gender, " +
                    "description, city, birthday, state, union_id, is_deleted, " +
                    "locked_until, created_time, updated_time, username, password, admin_password, should_introduce, mobile " +
            "FROM user " +
            "WHERE id IN ?1 ORDER BY FIELD (id, ?1) ",
            nativeQuery = true)
    List<UserInfo> findUserInfoInfosByIdInAndDeletedIsFalse(List<String> idList);

    UserInfo findTopByMobileAndDeletedIsFalse(String mobile);

    UserInfo findTopByMobileAndStateEqualsAndDeletedIsFalse(String mobile, Integer state);

    @Query(value = "SELECT u.id FROM user u LEFT JOIN user_identity i ON u.id = i.user_id WHERE u.nick_name LIKE ?1% ORDER BY i.identity_type DESC", nativeQuery = true)
    Page<String> findUserIdByNameOrderByIdentity(String username, Pageable pageable);
}
