package com.keepreal.madagascar.workflow.statistics.repository;

import com.keepreal.madagascar.workflow.statistics.model.IslandInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/
@Repository
public interface IslandInfoRepository extends JpaRepository<IslandInfo, String> {

    List<IslandInfo> findByIdInAndDeletedIsFalse(Set<String> ids);

}
