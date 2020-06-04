package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.repository.WechatOrderRepository;
import org.springframework.stereotype.Service;

@Service
public class WechatOrderService {

    private final LongIdGenerator idGenerator;
    private final WechatOrderRepository wechatOrderRepository;

    public WechatOrderService(LongIdGenerator idGenerator,
                              WechatOrderRepository wechatOrderRepository) {
        this.idGenerator = idGenerator;
        this.wechatOrderRepository = wechatOrderRepository;
    }

    /**
     * Inserts the wechat order.
     *
     * @param order {@link WechatOrder}.
     */
    public void insert(WechatOrder order) {
        order.setId(String.valueOf(this.idGenerator.nextId()));
        this.wechatOrderRepository.save(order);
    }

    /**
     * Retrieves wechat order by trade number.
     *
     * @param tradeNumber Trade number.
     * @return {@link WechatOrder}.
     */
    public WechatOrder retrieveByTradeNumber(String tradeNumber) {
        return this.wechatOrderRepository.findTopByTradeNumberAndDeletedIsFalse(tradeNumber);
    }

    /**
     * Update the wechat order.
     *
     * @param order {@link WechatOrder}.
     */
    public void update(WechatOrder order) {
        this.wechatOrderRepository.save(order);
    }

}
