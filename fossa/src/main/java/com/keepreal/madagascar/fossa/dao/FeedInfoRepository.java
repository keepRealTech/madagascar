package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.FeedInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-23
 **/

public interface FeedInfoRepository extends MongoRepository<FeedInfo, String> {


}
