package com.keepreal.madagascar.workflow.settler.repository;

import com.keepreal.madagascar.workflow.settler.model.WechatOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WechatOrderRepository extends JpaRepository<WechatOrder, String> {

    WechatOrder findByIdAndDeletedIsFalse(String id);

    List<WechatOrder> findAllByIdInAndDeletedIsFalse(Iterable<String> ids);

}