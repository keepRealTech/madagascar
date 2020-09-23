package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.AlipayOrder;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.repository.AlipayOrderRepository;
import org.springframework.stereotype.Service;

/**
 * Represents the alipay order service.
 */
@Service
public class AlipayOrderService {

    private final LongIdGenerator idGenerator;
    private final AlipayOrderRepository alipayOrderRepository;

    /**
     * Constructs the alipay order service.
     *
     * @param idGenerator           {@link LongIdGenerator}.
     * @param alipayOrderRepository {@link AlipayOrderRepository}.
     */
    public AlipayOrderService(LongIdGenerator idGenerator,
                              AlipayOrderRepository alipayOrderRepository) {
        this.idGenerator = idGenerator;
        this.alipayOrderRepository = alipayOrderRepository;
    }

    /**
     * Inserts the wechat order.
     *
     * @param order {@link AlipayOrder}.
     * @return {@link AlipayOrder}.
     */
    public AlipayOrder insert(AlipayOrder order) {
        order.setId(String.valueOf(this.idGenerator.nextId()));
        return this.alipayOrderRepository.save(order);
    }

    /**
     * Retrieves alipay order by order id.
     *
     * @param id Order id.
     * @return {@link AlipayOrder}.
     */
    public AlipayOrder retrieveById(String id) {
        return this.alipayOrderRepository.findByIdAndDeletedIsFalse(id);
    }

    /**
     * Retrieves alipay order by order trade number.
     *
     * @param tradeNumber Order trade number.
     * @return {@link AlipayOrder}.
     */
    public AlipayOrder retrieveByTradeNumber(String tradeNumber) {
        return this.alipayOrderRepository.findTopByTradeNumberAndDeletedIsFalse(tradeNumber);
    }

    /**
     * Update the alipay order.
     *
     * @param order {@link AlipayOrder}.
     * @return {@link AlipayOrder}.
     */
    public AlipayOrder update(AlipayOrder order) {
        return this.alipayOrderRepository.save(order);
    }

}
