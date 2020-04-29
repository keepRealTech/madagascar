package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.FeedInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-29
 **/

@Repository
public interface FeedInfoRepository extends JpaRepository<FeedInfo, Long> {
}
