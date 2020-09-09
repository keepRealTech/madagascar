package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.CommentInfo;
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
public interface CommentInfoRepository extends MongoRepository<CommentInfo, String> {

    Page<CommentInfo> getCommentInfosByFeedIdAndDeletedIsFalseOrderByCreatedTimeDesc(String feedId, Pageable pageable);

    CommentInfo findByIdAndDeletedIsFalse(String id);

    List<CommentInfo> findByIdInAndDeletedIsFalse(Iterable<String> ids);

}
