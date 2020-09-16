package com.keepreal.madagascar.workflow.update_payment.repository;

import com.keepreal.madagascar.workflow.update_payment.model.WechatOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WechatOrderRepository extends JpaRepository<WechatOrder, String> {

    @Query(value = "SELECT id FROM wechat_order WHERE (state = 1 OR state = 2) AND created_time > ?1", nativeQuery = true)
    List<String> findIdByState(long timestamp);
}
