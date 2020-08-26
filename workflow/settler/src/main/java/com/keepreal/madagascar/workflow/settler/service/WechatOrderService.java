package com.keepreal.madagascar.workflow.settler.service;

import com.keepreal.madagascar.workflow.settler.model.WechatOrder;
import com.keepreal.madagascar.workflow.settler.repository.WechatOrderRepository;
import org.springframework.stereotype.Service;

@Service
public class WechatOrderService {

    private final WechatOrderRepository wechatOrderRepository;

    public WechatOrderService(WechatOrderRepository wechatOrderRepository) {
        this.wechatOrderRepository = wechatOrderRepository;
    }

    /**
     * Retrieves wechat order by order id.
     *
     * @param id Order id.
     * @return {@link WechatOrder}.
     */
    public WechatOrder retrieveById(String id) {
        return this.wechatOrderRepository.findByIdAndDeletedIsFalse(id);
    }

    /**
     * Update the wechat order.
     *
     * @param order {@link WechatOrder}.
     * @return {@link WechatOrder}.
     */
    public WechatOrder update(WechatOrder order) {
        return this.wechatOrderRepository.save(order);
    }

}
