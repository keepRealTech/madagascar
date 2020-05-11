package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.CommentInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-29
 **/

@Repository
public interface CommentInfoRepository extends JpaRepository<CommentInfo, String> {

    Page<CommentInfo> getCommentInfosByFeedIdAndDeletedIsFalseOrderByCreatedTimeDesc(String feedId, Pageable pageable);

    CommentInfo findByIdAndDeletedIsFalse(String id);

}
