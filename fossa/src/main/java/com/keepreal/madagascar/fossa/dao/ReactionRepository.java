package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.ReactionInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Repository
public interface ReactionRepository extends MongoRepository<ReactionInfo, String> {
    Page<ReactionInfo> findReactionInfosByFeedId(String feedId, Pageable pageable);

    ReactionInfo findTopByFeedIdAndUserId(String feedId, String userId);

    boolean existsByUserIdAndFeedIdAndReactionTypeListContains(String userId, String feedId, Integer reactionType);
}
