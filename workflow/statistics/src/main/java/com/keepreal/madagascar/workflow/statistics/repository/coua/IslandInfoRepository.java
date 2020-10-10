package com.keepreal.madagascar.workflow.statistics.repository.coua;

import com.keepreal.madagascar.workflow.statistics.model.coua.IslandInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/
@Repository
public interface IslandInfoRepository extends JpaRepository<IslandInfo, String> {

    List<IslandInfo> findByIdInAndDeletedIsFalse(Iterable<String> ids);

}
