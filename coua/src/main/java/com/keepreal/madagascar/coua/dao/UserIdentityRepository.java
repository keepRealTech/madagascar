package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.UserIdentity;
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
public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {

    @Query(value = "SELECT identity_type FROM user_identity WHERE user_id = ?1 AND is_deleted = FALSE", nativeQuery = true)
    List<Integer> getUserIdentityTypesByUserId(Long userId);
}
