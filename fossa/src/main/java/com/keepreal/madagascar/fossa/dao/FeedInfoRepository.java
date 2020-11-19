package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.FeedInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-29
 **/

@Repository
public interface FeedInfoRepository extends MongoRepository<FeedInfo, String> {

    FeedInfo findFeedInfoByIdAndDeletedIsFalse(String id);

    FeedInfo findFeedInfoById(String id);

    FeedInfo findTopByUserIdAndDeletedIsFalseOrderByCreatedTimeDesc(String userId);

    List<FeedInfo> findAllByIdInAndDeletedIsFalseOrderByCreatedTimeDesc(Iterable<String> ids);

    List<FeedInfo> findAllByIdInAndDeletedIsFalse(Iterable<String> ids);

    List<FeedInfo> findAllByIdInAndDeletedIsFalseOrderByUpdatedTimeDesc(Iterable<String> ids);

    Page<FeedInfo> findAllByFeedGroupIdAndDeletedIsFalseOrderByCreatedTimeDesc(String id, Pageable pageable);

    Page<FeedInfo> findAllByFeedGroupIdAndMultiMediaTypeAndDeletedIsFalseOrderByCreatedTimeDesc(String id, String mediaType, Pageable pageable);

    FeedInfo findTopByIslandIdAndIsTopIsTrueAndDeletedIsFalse(String id);

}
