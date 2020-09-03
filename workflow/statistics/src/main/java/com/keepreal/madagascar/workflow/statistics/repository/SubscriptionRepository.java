package com.keepreal.madagascar.workflow.statistics.repository;

import com.keepreal.madagascar.workflow.statistics.model.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Page<Subscription> findAllByStateAndCreatedTimeBetweenAndDeletedIsFalse(Integer state, Long timestampAfter, Long timestampBefore, Pageable pageable);

}
