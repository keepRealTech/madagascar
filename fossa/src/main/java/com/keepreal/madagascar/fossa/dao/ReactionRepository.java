package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.ReactionInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Repository
public interface ReactionRepository extends JpaRepository<ReactionInfo, Long> {

    List<ReactionInfo> findReactionInfosByFeedIdAndUserIdAndReactionTypeIn(Long feedId, Long userId, List<Integer> reactionTypeList);

    Page<ReactionInfo> findReactionInfosByFeedId(Long feedId, Pageable pageable);
}
