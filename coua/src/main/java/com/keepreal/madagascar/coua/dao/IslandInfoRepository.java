package com.keepreal.madagascar.coua.dao;

import com.keepreal.madagascar.coua.model.IslandInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Repository
public interface IslandInfoRepository extends JpaRepository<IslandInfo, Long> {

    IslandInfo findByIslandNameAndDeletedIsFalse(String islandName);
}
