package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.RepostInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-05-06
 **/

@Repository
public interface RepostRepository extends JpaRepository<RepostInfo, String> {

    Page<RepostInfo> findRepostInfosByFromId(String fromId, Pageable pageable);
}
