package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.vanga.model.WechatOrder;
import com.keepreal.madagascar.vanga.model.WechatOrderType;
import com.keepreal.madagascar.vanga.repository.WechatOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * @return {@link WechatOrder}.
     */
    public WechatOrder insert(WechatOrder order) {
        order.setId(String.valueOf(this.idGenerator.nextId()));
        return this.wechatOrderRepository.save(order);
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
     * Retrieves wechat order by order trade number.
     *
     * @param tradeNumber Order trade number.
     * @return {@link WechatOrder}.
     */
    public WechatOrder retrieveByTradeNumber(String tradeNumber) {
        return this.wechatOrderRepository.findTopByTradeNumberAndDeletedIsFalse(tradeNumber);
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

    /**
     * Retrieves the wechat order for question payment.
     *
     * @param feedId Feed id.
     * @return {@link WechatOrder}.
     */
    public WechatOrder retrieveByQuestionId(String feedId) {
        return this.wechatOrderRepository.findByPropertyIdAndTypeAndDeletedIsFalse(feedId, WechatOrderType.PAYQUESTION.getValue());
    }

    public Page<WechatOrder> retrieveByUserIdPageable(String userId, Pageable pageable) {
        return this.wechatOrderRepository.findAllByUserId(userId, pageable);
    }

    public void updateAll(List<WechatOrder> orders) {
        this.wechatOrderRepository.saveAll(orders);
    }

}
