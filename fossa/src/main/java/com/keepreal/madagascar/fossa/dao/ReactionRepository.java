package com.keepreal.madagascar.fossa.dao;

import com.keepreal.madagascar.fossa.model.ReactionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Repository
public interface ReactionRepository extends JpaRepository<ReactionInfo, Long> {

}
